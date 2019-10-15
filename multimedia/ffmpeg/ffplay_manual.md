# ffplay工具命令介绍
ffplay 是一款使用 FFmpeg 库(用于转换视频和音频文件, 官方网址为 https://ffmpeg.org) 和 SDL 库(Simple DirectMedia Layer, 一个跨平台的开发库, 用于通过 OpenGL 和 Direct3D, 提供对音频, 鼠标, 操纵杆和图形硬件的低层访问, 官方网址为 https://www.libsdl.org), 主要用于各种 FFmpeg API 的测试平台.

## 安装
到官网上下载编译好的对应平台的二进制文件,[官网](https://evermeet.cx/ffmpeg/)

## 播放音频

### 播放PCM
播放PCM文件时，需要指定PCM的格式
```
ffplay -f s16be -ac 2 -ar 44100 -i shinian_qingchang.pcm
```

### 播放WAV
```
ffplay -i shinian_qingchang.wav
```

### 播放mp3
```
ffplay -i shinian_qingchang_1.mp3
```



