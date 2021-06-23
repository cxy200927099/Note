[TOC]
# OpenGLES and EGL

## 概念
### OpenGLES
[官网](https://www.khronos.org/opengles/)
是OpenGL for Embedded Systems设计的一套三维图形API，针对手机，PDA和游戏主机等嵌入式设备使用，由Khornos组织定义并维护，这个组织是图形软硬件行业协会，主要关注图形和多媒体方面的标准制定

### OpenGLES2.0渲染管线
![OpenGLES2.0渲染管线](imgs/openglES20_pipeline.png)
#### VertexShader:
顶点着色器，是一个可编程的处理单元。每个顶点执行一次，主要根据原始的顶点信息和其他属性信息传入顶点着色器，对顶点进行变换(平移，旋转，缩放)操作，光照计算，产生纹理坐标，颜色，点位置信息，传入下一阶段进行处理

##### 顶点着色器中变量类型attribute，uniform，varying
- attribute
指3D物体中每个顶点各自不同的信息所属的变量，一般顶点的位置，颜色，法向量都是以这种类型传入顶点着色器的
- uniform
对于同一组顶点组成的3D物体中所有顶点都相同的量，比如当前光源位置，摄像机的位置，投影变化矩阵
- varying
从顶点着色其中生产出的并传递到片元着色器中的数据变量，比如顶点坐标，纹理坐标，片元颜色
**注意: 这个易变变量在片元着色器中也需要声明**
#### PrimitiveAssembly:
图元装配，对一个个顶点按照绘制方式组装起来，比如简单的三角形，需要3个顶点按照绘制的顺序组合成一个图元
#### Rasterization:
将三维空间中的图元，进行转换投影到二维的屏幕空间中，得到一个个片元(每个片元可以理解为候选像素，不能称为像素是因为最终三维空间中可能存在遮挡的情况，这样很多个片元可能对应到frameBuffer中同一个像素),每个片元包含对应的顶点坐标，顶点颜色，顶点纹理坐标，以及顶点的深度信息
#### FragmentShader:
片元着色器，也是一个可编程的处理单元，每个片元(候选像素)执行一次，在这过程中进行特殊的处理得到当前片元的颜色值，通过gl_FragColor接收最终的处理结果
#### Per-FragmentOperations:
在片元着色器处理完成之后，接下来是对每一个片元进行一系列操作，经过光栅化之后得到的(x,y)对应屏幕上的坐标只能操作frameBuffer中(x,y)位置的像素
这一系列操作流程如下
![逐片元操作步骤](imgs/openglES20_pipeline_per_fragmentOperations.png)

##### Pixel Ownership Test
决定当前frameBuffer中的(x,y)位置的像素是否属于当前opengl es context，不属于就被丢弃
##### scissor Test
剪裁测试，如果成行启用了剪裁测试，opengl ES会检查每个片元在frameBuffer中的位置，在剪裁窗口之外的，则丢弃
##### Stencil Test
模板测试，将绘制区域限定在一定范围内，一般用在湖面倒影，镜像等场合
##### Depth Test
将输入片元的深度值与frameBuffer中当前位置存储的深度值作比较，若输入的深度值大于当前存储的深度值，则丢弃
##### Blending Test
混合测试，若程序开启了alpha混合，则根据混合因子将新输入的片元与frameBuffer中当前位置片元进行混合，否则直接覆盖
##### Dithering Test
抖动测试，允许只用少量的颜色模拟出更宽的颜色范围，使得颜色视觉效果更加丰富，比如使用白色和黑色模拟出一种过渡的灰色
缺点: 会损失一部分分辨率，现在原生颜色都很丰富的显示设备时代，一般都不需要启动抖动
#### Framebuffer:
一个用于存储片元信息的buffer


2. 顶点着色器可以传入哪些内容
    顶点坐标，顶点颜色信息，顶点变换矩阵，纹理变换矩阵
3. opengles 如何剔除坏像素点
    改变纹理变换矩阵,
## MVP矩阵，先平移后旋转和先旋转后平移有不同吗
mvp的几个矩阵意义:
### 物体矩阵 modelMatrix
这个矩阵包含了物体的变换(平移，旋转，缩放)信息
### 观察者矩阵 viewMatrix
这个矩阵就是包含了观察者(相机)的位置，朝向等信息
### 投影矩阵 projectMatrix
定义了最后三维空间如何投影到二维空间的信息，主要有正交矩阵和透视矩阵，这个是椎体的大小，比如上下左右观察的角度多大，远近平面的距离是什么样的，只有在视椎体中的物体才能被看得见，视椎体之外的物体会被裁剪
其中正交矩阵就相当于视椎体 远平面和近平面 一样大：这种投影方式不会有近大远小的视觉效果
透视投影也就是符合人眼观察的效果，有近大远小的效果

### 一个物体经过变换到屏幕的流程，
mvMatrix = viewMatrix * modelMatrix;
mvpMatrix = projectMatrix * mvMatrix;
可以这样理解，一开始相机和物体都有一个基础的参考坐标系，也就是世界坐标系，相机是可以平移移动旋转的，要让相机观察的世界也发生变化，那就相当于需要把世界之内的物体也需要跟着相机的变化而变化，所以物体自身的矩阵modelMatrix需要 左乘以 相机的矩阵viewMatrix；投影坐标系就定义了以什么样的方式来观察世界，投影矩阵 projectMatrix 就定义了这种观察的方式，最后也需要 mvMatrix 左乘以 projectMatrix，这一系列变换后就可以得到物体最终在什么样的观察方式下看到的结果 mvpMatrix

### 在来解释物体平移旋转缩放的问题
物体的平移，旋转，缩放都是在世界坐标系中的，刚开始的时候 物体坐标系与世界坐标系重合
- 缩放变换不改变坐标轴的走向，也不改变原点的位置，所以两个坐标系仍然重合。
- 旋转变换改变坐标轴的走向，但不改变原点的位置，所以两个坐标系坐标轴不再处于相同走向。
- 平移变换不改变坐标轴走向，但改变原点位置，两个坐标系原点不再重合。
正确合理的变换顺序 缩放 -> 旋转 -> 平移

- 先平移后旋转 和 先旋转后平移效果不一样的
因为opengl中对于 平移旋转缩放 都是采用矩阵来进行变换的，而矩阵的乘法本身不满足交换律，先后顺序不一样，结果也不一样


### EGL
EGL是一套介于opengl和本地平台窗口系统之间的接口，opengl通过egl将图形绘制到本地窗口中；egl处理图形上下文管理，surface/Buffer的绑定和渲染同步，并使用其他Khronos API启用高性能，加速的混合模式2D和3D渲染

EGL中的概念
Surface
Display
Context


## android OpenGL坐标系
android屏幕坐标: 左上角(0,0),右下角(width, height)
顶点坐标系: 左下角(-1,-1),右上角(1, 1)
纹理坐标系: 左下角(0，0),右上角(1, 1)，表示的坐标是映射到纹理图片的坐标，即图片左下角为0，0，右上角为1，1
viewport坐标系: 是针对屏幕的，与屏幕坐标系一样，viewport是表示观察3d空间的窗口大小，与屏幕一样就能观察整个(-1,-1)到(1,1)的内容


## 离屏渲染

### renderToTexture
- 初始化
```java
int[] framebufferIds = new int[1];
// 申请frameBuffer
GLES20.glGenFramebuffers(1, framebufferIds, 0);
// 绑定到当前申请的frameBuffer
GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferIds[0]);
this.framebufferId = framebufferIds[0];
// 将textureId绑定到申请的FrameBuffer
GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
        GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
        destTextureId, 0);
```

- 使用
```java
private void beforeDrawFrame() {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.framebufferId);
    GLUtils.checkGlError("glBindFramebuffer bind");
}
private void afterDrawFrame() {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    GLUtils.checkGlError("glBindFramebuffer unbind");
}

public void renderToTexture(int srcTextureId, int destTextureId, int width, int height){

    if(width != mWidth || height != mHeight || destTextureId != lastDestTextureId){
        initFrameBuffer(destTextureId);
        GLUtils.checkGlError("initFrameBuffer");
        mWidth = width;
        mHeight = height;
        lastDestTextureId = destTextureId;
        if(destTextureId != lastDestTextureId){
            if(lastDestTextureId != -1){
                GLES20.glDeleteTextures(1, new int[]{lastDestTextureId},0);
                lastDestTextureId = -1;
            }
        }
    }
    //绑定frameBuffer
    beforeDrawFrame();
    GLES20.glViewport(0, 0, mWidth, mHeight);
    GLUtils.checkGlError("glViewport before");
    //这时候绘制的内容最后都到 destTextureId上面去了
    mQuard.drawSelf(GLES20.GL_TEXTURE0, srcTextureId);
    //解绑frameBuffer
    afterDrawFrame();
}
```

### renderToBuffer



## 多线程渲染
多线程渲染就是开几个线程用于资源上传和着色器编译，避免主线程因为传输效率或CPU编译时间导致CPU GPU闲置
```cpp
// 首先创建用于共享的eglContext
EGLContext shareContext = eglCreateContext(eglDisplay, config, EGL_NO_CONTEXT, attrib2_list);
...
// 用这个共享context创建出其他线程渲染所需要的context
EGLContext eglContext = eglCreateContext(eglDisplay, config, shareContext, attrib2_list);


// 多线程渲染流程
// 将eglContext绑定到当前线程 thread1
eglMakeCurrent(display, surface, surface, eglContext);
// 执行 opengl相关的操作，一般用于做资源上传着色器编译
// glTexImage2D()上传资源
// 将context从thread1解除绑定
eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
```


## openGL ES优化

### shader优化
#### 减少条件分支语句if/for的使用
GPU不擅长处理分支结构
1. 如下条件
```glsl
float4 a;
if (b > 1) {
  a = 1;
}else {
  a = 0.5;
}
```
可以用 step函数来优化,step的原形为
```glsl
genType step (float edge, genType x)
// 如果x < edge，返回0.0，否则返回1.0
```
2. 优化的结果
```glsl
float4 a;
float4 tmp = step(b, 1);
a = tmp * 0.5 + (1 - tmp);
```
#### 大的shader进行拆分
把大的Shader程序采裁剪成每个Surface所需要的，而不使用大而全的Shader程序，小而精的Shader程序通常运行得更快
#### 避免过多的varing变量
避免使用过多的varyings：在shader编程时，在Fragment Shader程序中，尽量节约使用varings；因为在VP与内存或FP与内存间传递varings时需要消耗内存带宽
#### 精度值
顶点处理器基于32位浮点值工作：Vertex Shader使用浮点表示整数。为了避免32位值，设置Vertex Shader程序的输出varing的精度为mediump或lowp。

### 纹理优化
高分辨率的纹理不仅从CPU传输到GPU会耗时，在GPU中也会暂用较大内存
- 尽量不要使用高分辨率的纹理
### 关闭mipmapping，打开纹理映射(mipmapping)，有时可能降低了渲染质量
### 对于大的网格，一个顶点被包含在多个三角形中，这样的顶点被处理的次数依赖调用的画图函数：
- glDrawElements：每个顶点仅被处理一次，效率更高。
- glDrawArrays：每个顶点数据在每一个使用它的三角形中被传输和处理一次
### 使用顶点缓冲对象(Vertex Buffer Objects)
  使用VBO时opengl es会将顶点数据缓存，这样可以避免每次调用draw的时候都需要传输顶点数据
### 避免 glReadPixels 的使用
即使读取很少像素，对性能影响也比较大，因为它暂停了pipline


## glReadPixels 的优化方案
[参考](https://www.mdeditor.tw/pl/pXo9)
当调用 glReadPixels 时，首先会影响 CPU 时钟周期，同时 GPU 会等待当前帧绘制完成，读取像素完成之后，才开始下一帧的计算，造成渲染管线停滞
### PBO
这个是OpenGLES 3.0才有的功能,与VBO类似，也会在GPU开辟缓冲区，不过这个缓冲区是用于存放图像数据
这样使用pbo后，glTextImage2D函数就不用从CPU传输数据到GPU，而是直接从PBO中去取数据
一般都是使用两个pbo来处理


### PBO的疑问
使用两个pbo，这两个pbo的地址是怎么联系在一起的，按照pbo的使用流程，纹理从pbo1中取数据，但是从cpu传递数据传到的pbo2；
同样的读取frameBuffer数据的时候，glReadPixels把数据读到pbo1，CPU调用 glMapBufferRange 获取的pbo2的指针地址去取数据，这是如何关联在一起的？

难道是由于经过两次 glBindBuffer 之后，两个pbo buffer的缓冲区地址就共用同一个？
```cpp
//将图像数据从帧缓冲区读回到 PBO 中
BEGIN_TIME("DownloadPixels glReadPixels with PBO")
glBindBuffer(GL_PIXEL_PACK_BUFFER, m_DownloadPboIds[index]);
glReadPixels(0, 0, m_RenderImage.width, m_RenderImage.height, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
END_TIME("DownloadPixels glReadPixels with PBO")

// glMapBufferRange 获取 PBO 缓冲区指针
BEGIN_TIME("DownloadPixels PBO glMapBufferRange")
// 这个操作之后，pbo1和pbo2的数据缓冲区是同一个地址？
glBindBuffer(GL_PIXEL_PACK_BUFFER, m_DownloadPboIds[nextIndex]);
```
### HardwareBuffer
AHardwareBuffer 读取显存（纹理）图像数据时，需要与 GLEXT 和 EGLEXT 配合使用 ，主要步骤：首先需要创建 AHardwareBuffer 和 EGLImageKHR 对象，然后将目标纹理（FBO 的颜色附着）与 EGLImageKHR 对象绑定，渲染结束之后便可以读取纹理图像。









