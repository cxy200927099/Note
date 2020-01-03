# 爬虫入门
什么是爬虫？


## go框架
[goquery](https://github.com/PuerkitoBio/goquery): 一个实现了jquery功能的go函数库，用于解析html网页
[gocolly/colly]() a lightning fast and elegant Scraping Framework

## python框架

scrapy







对比两次请求的结果

https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&ct=201326592&is=&fp=result&queryWord=%E9%A3%8E%E6%99%AF&cl=2&lm=-1&ie=utf-8&oe=utf-8&adpicid=&st=-1&z=&ic=0&hd=&latest=&copyright=&word=%E9%A3%8E%E6%99%AF&s=&se=&tab=&width=&height=&face=0&istype=2&qc=&nc=1&fr=&expermode=&force=&pn=90&rn=30&gsm=&1575949149434=


https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&ct=201326592&is=&fp=result&queryWord=%E9%A3%8E%E6%99%AF&cl=2&lm=-1&ie=utf-8&oe=utf-8&adpicid=&st=-1&z=&ic=0&hd=&latest=&copyright=&word=%E9%A3%8E%E6%99%AF&s=&se=&tab=&width=&height=&face=0&istype=2&qc=&nc=1&fr=&expermode=&force=&pn=120&rn=30&gsm=&1575949316061=

只有两个字段发生改变，经过分析后发现这两个字段如下
pn: 图片数量，发现滑动鼠标滚轮的时候，这个会发起两次请求，每次请求pn的值加30
1575949149434:这个是当前请求的时间戳 ms



http://www.n63.com/n_chinam/183club



gocolly如何更方便的 爬取多级页面
比如我有个页面A的url，需要重A中解析获取一堆的 链接，这些链接跳转到 B页面
同样B页面存在分页的情况，需要发起请求获取所有B页面中想要的内容列表，这些内容点击之后跳转到C页面
最后从C页面中解析出最终的可以直接下载的地址，最后执行下载
同时需要从A页面中保存一些数据，这个在B和C中都没一存在的，
对与这种案例，怎样做比较方便？
我现在是都用同一个gocolly的collector实例，然后里面分多个 onHTML回调来处理不同页面，发现这样做有点混乱
而且我现在只用的单协程，怕用多协程之后，会乱onHTML的回调顺序不受控制，因为我需要用到context，把页面A的有些数据传到页面C的请求结果中


/html/body/center/table/tbody/tr/td/table/tbody/tr[2]/td[2]/text()[2]
/html/body/center/table/tbody/tr/td/table/tbody/tr[3]/td[2]/text()[2]
//*[@id="tablen63"]/tbody/tr[3]/td[2]/text()[2]

