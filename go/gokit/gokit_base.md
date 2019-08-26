# gokit_base
本文主要记录gokit的基础用法,以一个简单的stringService为基础，[原文参考这里](http://gokit.io/examples/stringsvc.html)

## simpleService
在这里，我们需要创建一个main.go 文件

### 业务逻辑
首先需要搞清楚我们的服务提供什么功能，使用gokit，需要提供一个interface，如下所示
```
// StringService provides operations on strings.
import "context"

type StringService interface {
	Uppercase(string) (string, error)
	Count(string) int
}
```
接着需要实现定义的接口
```
import (
	"context"
	"errors"
	"strings"
)

type stringService struct{}

func (stringService) Uppercase(s string) (string, error) {
	if s == "" {
		return "", ErrEmpty
	}
	return strings.ToUpper(s), nil
}

func (stringService) Count(s string) int {
	return len(s)
}

// ErrEmpty is returned when input string is empty
var ErrEmpty = errors.New("Empty string")
```

### 请求与响应
在gokit中主要的消息传递模式是 rpc，所以我们interface中的每一个method其实都是为了给远程调用而创建的，
对于每一个方法，我们定义 request 和 response 结构体，来描述所有的输入和输出，如下:
```
type uppercaseRequest struct {
	S string `json:"s"`
}

type uppercaseResponse struct {
	V   string `json:"v"`
	Err string `json:"err,omitempty"` // errors don't JSON-marshal, so we use a string
}

type countRequest struct {
	S string `json:"s"`
}

type countResponse struct {
	V int `json:"v"`
}
```

### Endpoints
gokit把大部分的功能抽象到一个称为 endpoint 的东西中，endpoint的定义在gokit中，这里是一个函数指针，如下
```
type Endpoint func(ctx context.Context, request interface{}) (response interface{}, err error)
```
endpoint表示一次rpc调用，即调用一个我们所提供的方法，我们需要编写 adapter来将我们服务的方法 转为 endpoint，
每一个adapter接收一个StringService然后返回一其中一个方法对应的endpoint，如下代码及为编写的adapter
```
import (
	"context"
	"github.com/go-kit/kit/endpoint"
)

func makeUppercaseEndpoint(svc StringService) endpoint.Endpoint {
  //返回endpoint结构
	return func(_ context.Context, request interface{}) (interface{}, error) {
    //强转为我们定义的uppercaseRequest结构
		req := request.(uppercaseRequest)
		v, err := svc.Uppercase(req.S)
		if err != nil {
			return uppercaseResponse{v, err.Error()}, nil
		}
		return uppercaseResponse{v, ""}, nil
	}
}

func makeCountEndpoint(svc StringService) endpoint.Endpoint {
	return func(_ context.Context, request interface{}) (interface{}, error) {
		req := request.(countRequest)
		v := svc.Count(req.S)
		return countResponse{v}, nil
	}
}
```

### transports
经过上面步骤，现在我们可以对外提供服务了，gokit中提供了thrift，自定义json通过http接口，以满足日常大家需要
在这里我们通过http接口提供json向外提供服务，所以需要实现服务的返回值,gokit中在transport/http 包下有相关的helper struct
```
import(
  “context”
  "encoding/json"
  "log"
  "net/http"
  httptransport "github.com/go-kit/kit/transport/http"
)

func main(){
  svc := stringService{}

  //定义uppercase接口的处理函数
  uppercaseHandler := httptransport.NewServer(
		makeUppercaseEndpoint(svc),
		decodeUppercaseRequest,
		encodeResponse,
	)

  //定义count接口的处理函数
	countHandler := httptransport.NewServer(
		makeCountEndpoint(svc),
		decodeCountRequest,
		encodeResponse,
	)

	http.Handle("/uppercase", uppercaseHandler)
	http.Handle("/count", countHandler)
	log.Fatal(http.ListenAndServe(":8080", nil))
}

//解析请求参数
func decodeUppercaseRequest(_ context.Context, r *http.Request) (interface{}, error) {
	var request uppercaseRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}
	return request, nil
}

//解析请求参数
func decodeCountRequest(_ context.Context, r *http.Request) (interface{}, error) {
	var request countRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}
	return request, nil
}

//json编码
func encodeResponse(_ context.Context, w http.ResponseWriter, response interface{}) error {
	return json.NewEncoder(w).Encode(response)
}

```

### stringsvc1
完成代码工程参考这里 [stringsvc1](../examples/gokit/stringsvc1/main.go)

```
依赖安装：
go get -u github.com/go-kit/kit/endpoint
go get -u github.com/go-kit/kit/transport/http

编译:
go build -o stringsvc1 main.go

server运行：
./stringsvc1

```
向服务发送数据
```
request:curl -XPOST -d'{"s":"hello, world"}' localhost:8080/uppercase
response: {"v":"HELLO, WORLD"}

request:curl -XPOST -d'{"s":"hello, world"}' localhost:8080/count
response: {"v":12}
```

## Middlewares

### 分层设计
随着服务endpoints的增加，将gokit项目的调用流程分层设计可以提供项目的可读性，减少bug发生的几率；
上一个例子stringsvc1 把所有的层都放到一个main file中，在添加新功能之前，我们先把这个项目简单分下层
将main中以下代码抽离出来，剩余保留在main中
service.go
```
type StringService
type stringService
var ErrEmpty
```

transport.go
```
func makeUppercaseEndpoint
func makeCountEndpoint
func decodeUppercaseRequest
func decodeCountRequest
func encodeResponse
type uppercaseRequest
type uppercaseResponse
type countRequest
type countResponse
```

### Transport logging
在这里可以使用middleware 来给transport 加上log
middleware的定义在gokit中如下所示:
```
type Middleware func(Endpoint) Endpoint
```
其输入一个 endpoint 然后输出一个endpoint，因此在这其中就可以做任何需要做的事情，下面介绍一个基础的log middleware是如何实现的
```
func loggingMiddleware(logger log.Logger) Middleware {
	return func(next endpoint.Endpoint) endpoint.Endpoint {
		return func(_ context.Context, request interface{}) (interface{}, error) {
			logger.Log("msg", "calling endpoint")
			defer logger.Log("msg", "called endpoint")
			return next(request)
		}
	}
}
```
下面使用go-kit log包来代替 go标准包的log，main.go文件中需要引入如下包,将log.Fatal删除
```
import (
 "github.com/go-kit/kit/log"
)
```
初始化handler的时候需要先创建log中间件
```
logger := log.NewLogfmtLogger(os.Stderr)

svc := stringService{}

var uppercase endpoint.Endpoint
uppercase = makeUppercaseEndpoint(svc)
uppercase = loggingMiddleware(log.With(logger, "method", "uppercase"))(uppercase)

var count endpoint.Endpoint
count = makeCountEndpoint(svc)
count = loggingMiddleware(log.With(logger, "method", "count"))(count)

uppercaseHandler := httptransport.NewServer(
	// ...
	uppercase,
	// ...
)

countHandler := httptransport.NewServer(
	// ...
	count,
	// ...
)

```
实践证明这项技术很有用，而不仅仅只用作log，许多go kit的组件都是基于middleware来实现的

### Application logging
对于一些log打印，经常需要打印很多信息如domain等其他参数，在这里通过包装stringService接口来实现，如下
```
type loggingMiddleware struct {
	logger log.Logger
	next   StringService
}

//实现接口Uppercase，然后最后调用log打印
func (mw loggingMiddleware) Uppercase(s string) (output string, err error) {
  //最后再调用打印
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "uppercase",
			"input", s,
			"output", output,
			"err", err,
			"took", time.Since(begin),
		)
	}(time.Now())

	output, err = mw.next.Uppercase(s)
	return
}

//实现接口Count，然后最后调用log打印
func (mw loggingMiddleware) Count(s string) (n int) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "count",
			"input", s,
			"n", n,
			"took", time.Since(begin),
		)
	}(time.Now())

	n = mw.next.Count(s)
	return
}
```
main.go中的修改
```
import (
	"os"

	"github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"
)

func main() {
	logger := log.NewLogfmtLogger(os.Stderr)

	var svc StringService
	svc = stringService{}
	svc = loggingMiddleware{logger, svc}

	// ...

	uppercaseHandler := httptransport.NewServer(
		// ...
		makeUppercaseEndpoint(svc),
		// ...
	)

	countHandler := httptransport.NewServer(
		// ...
		makeCountEndpoint(svc),
		// ...
	)
}
```
使用endpoint middleware来解决传输域问题，例如断路和速率限制。 使用服务中间件来解决业务领域问题，例如日志记录和检测。

### Application instrumentation
在Go工具包metrics中，instrumentation负责记录有关服务运行时行为的统计信息，统计任务处理数目，记录请求的耗时等
这里我们可以像使用log middleware一样来使用instrumentation
```
type instrumentingMiddleware struct {
	requestCount   metrics.Counter
	requestLatency metrics.Histogram
	countResult    metrics.Histogram
	next           StringService
}

func (mw instrumentingMiddleware) Uppercase(s string) (output string, err error) {
	defer func(begin time.Time) {
		lvs := []string{"method", "uppercase", "error", fmt.Sprint(err != nil)}
		mw.requestCount.With(lvs...).Add(1)
		mw.requestLatency.With(lvs...).Observe(time.Since(begin).Seconds())
	}(time.Now())

	output, err = mw.next.Uppercase(s)
	return
}

func (mw instrumentingMiddleware) Count(s string) (n int) {
	defer func(begin time.Time) {
		lvs := []string{"method", "count", "error", "false"}
		mw.requestCount.With(lvs...).Add(1)
		mw.requestLatency.With(lvs...).Observe(time.Since(begin).Seconds())
		mw.countResult.Observe(float64(n))
	}(time.Now())

	n = mw.next.Count(s)
	return
}
```
service中的实现
```
import (
	stdprometheus "github.com/prometheus/client_golang/prometheus"
	kitprometheus "github.com/go-kit/kit/metrics/prometheus"
	"github.com/go-kit/kit/metrics"
)

func main() {
	logger := log.NewLogfmtLogger(os.Stderr)

	fieldKeys := []string{"method", "error"}
	requestCount := kitprometheus.NewCounterFrom(stdprometheus.CounterOpts{
		Namespace: "my_group",
		Subsystem: "string_service",
		Name:      "request_count",
		Help:      "Number of requests received.",
	}, fieldKeys)
	requestLatency := kitprometheus.NewSummaryFrom(stdprometheus.SummaryOpts{
		Namespace: "my_group",
		Subsystem: "string_service",
		Name:      "request_latency_microseconds",
		Help:      "Total duration of requests in microseconds.",
	}, fieldKeys)
	countResult := kitprometheus.NewSummaryFrom(stdprometheus.SummaryOpts{
		Namespace: "my_group",
		Subsystem: "string_service",
		Name:      "count_result",
		Help:      "The result of each count method.",
	}, []string{}) // no fields here

	var svc StringService
	svc = stringService{}
	svc = loggingMiddleware{logger, svc}
	svc = instrumentingMiddleware{requestCount, requestLatency, countResult, svc}

	uppercaseHandler := httptransport.NewServer(
		makeUppercaseEndpoint(svc),
		decodeUppercaseRequest,
		encodeResponse,
	)

	countHandler := httptransport.NewServer(
		makeCountEndpoint(svc),
		decodeCountRequest,
		encodeResponse,
	)

	http.Handle("/uppercase", uppercaseHandler)
	http.Handle("/count", countHandler)
	http.Handle("/metrics", promhttp.Handler())
	logger.Log("msg", "HTTP", "addr", ":8080")
	logger.Log("err", http.ListenAndServe(":8080", nil))
}
```

### 完整工程



## Calling other services








