# AWK
awk是一种处理文本的语言，在Linux中处理文本速度非常快，而且方便，语法与C语言类似
## 语法:
awk [选项参数] -f scriptfile var=value file(s)
- -F fs or --field-separator fs
指定输入文件折分隔符，fs是一个字符串或者是一个正则表达式，如-F:。
- -v var=value or --asign var=value
赋值一个用户定义变量。
- -f scripfile or --file scriptfile
从脚本文件中读取awk命令。

## 基本用法
log.txt
```
2 this is a test
3 Are you like awk
This's a test
10 There are orange,apple,mongo
```

- 实例
```
 # 每行按空格或TAB分割，输出文本中的1、4项
 $ awk '{print $1,$4}' log.txt
 ---------------------------------------------
 2 a
 3 like
 This's
 10 orange,apple,mongo

 # 格式化输出
 $ awk '{printf "%-8s %-10s\n",$1,$4}' log.txt
 ---------------------------------------------
 2        a
 3        like
 This's
 10       orange,apple,mongo
```

使用其他分隔符，如逗号
```
 # 使用","分割
 $  awk -F, '{print $1,$2}'   log.txt
 ---------------------------------------------
 2 this is a test
 3 Are you like awk
 This's a test
 10 There are orange apple
 # 或者使用内建变量
 $ awk 'BEGIN{FS=","} {print $1,$2}'     log.txt
 ---------------------------------------------
 2 this is a test
 3 Are you like awk
 This's a test
 10 There are orange apple
 # 使用多个分隔符.先使用空格分割，然后对分割结果再使用","分割
 $ awk -F '[ ,]'  '{print $1,$2,$5}'   log.txt
 ---------------------------------------------
 2 this test
 3 Are awk
 This's a
 10 There apple
```

## 读取awk脚本文件
在之前做的项目中，需要统计请求相关的信息，包括总的请求量，成功数量，失败数量，最大qps，最大qps的时间点等等，日志文件如下
log.txt
```log
2020-04-25 16:43:24 [Info] common.go:49 ReqStatistics--VideoId=8631C1FFCC2F91824C5C20D495E0680DC7E73EF2 ComingTimeStr=2020-4-25-16:43:22.904241634 ReqType=statistic_query CompleteTime=2020-4-25-16:43:24.678636830 timeStamp(ms)=1774 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:24 [Info] common.go:49 ReqStatistics--VideoId=86324ECAC460F64F4F174EA3565EA10BFF0DB5F2 ComingTimeStr=2020-4-25-16:43:23.382045041 ReqType=statistic_query CompleteTime=2020-4-25-16:43:24.720709293 timeStamp(ms)=1338 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:24 [Info] common.go:49 ReqStatistics--VideoId=863376E446BDA00403EF7480B48B20A6674920F2 ComingTimeStr=2020-4-25-16:43:24.446629255 ReqType=statistic_query CompleteTime=2020-4-25-16:43:24.804691435 timeStamp(ms)=358 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:25 [Info] common.go:49 ReqStatistics--VideoId=8632E94A7A8F35785BB06E2C552A04C191C670F2 ComingTimeStr=2020-4-25-16:43:23.886425666 ReqType=statistic_query CompleteTime=2020-4-25-16:43:25.135067253 timeStamp(ms)=1248 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:25 [Info] common.go:49 ReqStatistics--VideoId=86372C291CC71175E7917782857AAE0F89A036F2 ComingTimeStr=2020-4-25-16:43:25.374530979 ReqType=statistic_query CompleteTime=2020-4-25-16:43:25.880322126 timeStamp(ms)=505 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:25 [Info] common.go:49 ReqStatistics--VideoId=8634930F519DF164999D29F6B945434889D88FF2 ComingTimeStr=2020-4-25-16:43:24.892683089 ReqType=statistic_query CompleteTime=2020-4-25-16:43:25.957817399 timeStamp(ms)=1065 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:26 [Info] common.go:49 ReqStatistics--VideoId=863E387CEA14049358513B312F4E1BC124A9A4F2 ComingTimeStr=2020-4-25-16:43:26.391500747 ReqType=statistic_query CompleteTime=2020-4-25-16:43:26.712839400 timeStamp(ms)=321 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:26 [Info] common.go:49 ReqStatistics--VideoId=8638735DFCC559D5BDA18C14098BD72C3D0FB4F2 ComingTimeStr=2020-4-25-16:43:25.915625533 ReqType=statistic_query CompleteTime=2020-4-25-16:43:26.966022948 timeStamp(ms)=1050 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:27 [Info] common.go:49 ReqStatistics--VideoId=3942F18CE8A86700F445592E2FAC20B680F0FDE0 ComingTimeStr=2020-4-25-16:43:25.604681208 ReqType=statistic_query CompleteTime=2020-4-25-16:43:27.265298820 timeStamp(ms)=1660 IsSuccess=true CacheHit=false IsRealQuery=true
2020-04-25 16:43:27 [Info] common.go:49 ReqStatistics--VideoId=864544307C0070537F094F5A35BC4A114972C1F2 ComingTimeStr=2020-4-25-16:43:27.392300617 ReqType=statistic_query CompleteTime=2020-4-25-16:43:27.742151709 timeStamp(ms)=349 IsSuccess=true CacheHit=false IsRealQuery=true
```
首先需要获取到每一行中对应的日志属性，这里都是以'='来分割的，然后解析相关的内容

- awk脚本实现
static.awk
```awk
#!/bin/awk -f
BEGIN{
    max_qps=0
    total_cnt=0
    success_cnt=0
    failed_cnt=0
    real_query_cnt=0
    cache_hit_cnt=0
    req_time_max=0
    req_time_avg=0
    req_time_sum=0
}
{
    total_cnt += 1

    split($5, arr, "=")
    video_id=arr[2]

    split($6, arr, "=")
    coming_time=arr[2]
    split(arr[2], arr1, ".")
    s_time=arr1[1]
    if (s_time in map){
        cur_cnt = map[s_time]
        map[s_time] = cur_cnt+1
    }else
        map[s_time] = 1

    split($7, arr, "=")
    req_type=arr[2]

    split($8, arr, "=")
    complete_time=arr[2]

    split($9, arr, "=")
    timestamp=arr[2]
    req_time_sum += timestamp
    if (timestamp > req_time_max)
        req_time_max=timestamp

    split($10, arr, "=")
    is_success=arr[2]
    if (is_success == "true")
        success_cnt += 1
    else
        failed_cnt += 1

    split($11, arr, "=")
    cache_hit=arr[2]
    if (cache_hit == "true")
        cache_hit_cnt += 1

    split($12, arr, "=")
    is_real_query=arr[2]
    if (is_real_query == "true")
        real_query_cnt += 1

    #printf "%s %s %s %s %s %s %s %s\n", $5,$6,$7,$8,$9,$10,$11,$12
    #printf "%s %s %s %s %s %s %s %s\n", video_id,coming_time,req_type,complete_time,timestamp,is_success,cache_hit,is_real_query
}
END{
    for (key in map){
        #printf "map key:%s val:%d\n", key,map[key]
        if (map[key] >= max_qps){
            max_qps_ts = key
            max_qps = map[key]
        }
    }
    #printf "max_qps_ts:%s max_qps:%d\n", max_qps_ts,max_qps
    if (total_cnt != 0){
        req_time_avg= req_time_sum / total_cnt
    }

    msg=sprintf("total_cnt:%d\\nsuccess_cnt:%d\\nfailed_cnt:%d\\nreal_query_cnt:%d\\ncache_hit_cnt:%d\\nreq_time_max:%d\\nreq_time_avg:%d\\nmax_qps_ts:%s\\nmax_qps:%d\\n",total_cnt,success_cnt,failed_cnt,real_query_cnt,cache_hit_cnt,req_time_max,req_time_avg,max_qps_ts,max_qps)
    print msg
}
```

- 执行统计
```
$ awk -f static.awk log.txt
total_cnt:10\nsuccess_cnt:10\nfailed_cnt:0\nreal_query_cnt:10\ncache_hit_cnt:0\nreq_time_max:1774\nreq_time_avg:966\nmax_qps_ts:2020-4-25-16:43:25\nmax_qps:3\n

```

> 说明:其中统计最大qps的时候需要用到map，awk中map使用也很简单，不需要定义，直接使用即可

> 关于awk的返回值，在实际应用中，我们通常都是结合shell来使用awk，就比如上面的脚本，我们希望把统计的结果 msg返回去，直接 print msg 就可以，如果print多次，那么每一个print的结果都会作为实际的返回结果；关于awk返回更多复杂的数据结构，后续学习到在更新

