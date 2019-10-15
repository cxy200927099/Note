# ffmpeg常用命令大全

## 提取视频中的mp3文件：
```
ffmpeg -i 01.mp4 -acodec libmp3lame -vn 01.mp3
```
-i: 源： 01.mp4
  输出 ： 01.mp3


## 压缩视频：
```
ffmpeg -i test.mp4 -b 1M -s 1920x960 test_1080.mp4
```
-i: 输入文件
-b: 码率
-s: 分辨率



## 视频切片：
将 MP4文件切片成hls的M3u8片段 [参考](http://blog.csdn.net/jookers/article/details/21694957)
```
ffmpeg -i test.mp4 -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 test.m3u8
```
-hls_time 10: 指定每片长度为10s
-hls_list_size 0: 表示生成的m3u8的文件中，列出所有的片段信息，默认是5，那样后面切片的会覆盖前面的，这样就会导致播放的视频是在从倒数第5个片段开始播放
test.mp4 源MP4


## 视频转为 gif：
[参考这里](http://blog.csdn.net/happydeer/article/details/45727227)
```
ffmpeg -i ViewPager.mp4 -f gif viewpagerDemo.gif
```

## 截取一段视频：
```
ffmpeg -ss 00:46:28 -i "Morning_News.asf" -acodec copy -vcodec copy -t 00:03:25 output.asf
```
这行命令解释为：从文件 Morning_News.asf 第 46:28 分秒开始，截取 03: 25 的时间，其中视频和音频解码不变，输出文件名为 output.asf 


## 从视频中提取图片：
```
ffmpeg -ss 00:01:51 -i video_360_3D.mp4 -f image2 -y test2.jpg
ffmpeg -ss 10 -i input.flv -y -f image2  -vframes 100 -s 352x240 b-%03d.jpg 
```

参数解释:
-i  输入文件
-y  覆盖
-f  生成图片格式
-ss 开始截图时间 seconds or in hh:mm:ss[.xxx] 如果截图开始时间越接近篇尾，所花费的时间就会越长
-vframes  截图帧数 或者 使用 -t : 截图时长 seconds, or hh:mm:ss[.xxx]
-s  图片宽高比
b-%3d.jpg 格式化文件命名,会生成 b-001.jpg，b-002.jpg 等。

**注意：把-ss 10放到第一个参数的位置，速度比放到放到其他位置快，且不会出现如下错误**
```“[buffer @ 0x217c550] Buffering several frames is not supported. Please consume all available frames before adding a new one.”```


## 截取MP3
```
ffmpeg -i source_mp3.mp3 -ss 00:01:12 -t 00:01:42 -acodec copy output_mp3.mp3
```
* 源音频：source_mp3
* 开始位置：-ss
* 结束位置：-t
* 原始编码：-acodec


## 或M4a MP3 转 PCM
```
ffmpeg -i shinian_1.mp3 -f s16be -ar 44100 -ac 2 -acodec pcm_s16be shinian_1.pcm
```
转到PCM需要指定一些参数
-f: 设置文件格式s16be(16位大端)，还有s16le
-ar: 设置采样率 44100Hz
-ac: channel数，单通道还是多通道
-acodec: 编码格式

## 或M4a MP3 转 WAV
```
ffmpeg -i shinian_1.mp3 shinian_1.wav -y
```

## PCM 转 MP3
```
ffmpeg -f s16be  -ac 2 -ar 44100 -acodec pcm_s16be -i shinian_qingchang_1.pcm shinian_qingchang_1.mp3 -y
```


