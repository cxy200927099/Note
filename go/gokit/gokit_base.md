# gokit_base
本文主要记录gokit的基础用法,以一个简单的stringService为基础，[原文参考这里](http://gokit.io/examples/stringsvc.html)

## 创建工程
在这里，我们需要创建一个main.go 文件

## 业务逻辑
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

## 请求与响应
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

## Endpoints
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

## transports
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

## stringsvc1
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


