[TOC]
# 介绍
JVM: 是指通常的java虚拟机
DVM: android引入的Davik虚拟机
ART: android4.4引入的新一代虚拟机

## DVM vs JVM
- 共同点
  都是解释执行,采用JIT(Just In Time，即时编译技术)直接转成会变代码，都是每个进程运行一个VM

- 不同点:
1. dvm运行的是.dex文件，jvm运行的是.class文件
  - 多个class组成了dex文件，dvm存在方法数超过65535时报错，后来引入了multiDex技术
  - dex文件可以去除class文件的冗余信息，class文件存在很多的冗余信息，dex工具会去除冗余信息(多个class中的字符串常量合并为一个，比如对于Ljava/lang/Oject字符常量，每个class文件基本都有该字符常量，存在很大的冗余)，把所有的.class文件整合到.dex文件中。减少了I/O操作，提高了类的查找速度
2. dvm基于寄存器运行，而jvm基于虚拟机栈
  由于jvm目的是做到跨平台，所以采用虚拟机栈的方式，dvm基本是在arm上运行，因此进一步直接使用寄存器存取，可以结合硬件最大优化，执行速度上比较快


## ART vs DVM
1.dex
java程序编译成class后，dx工具将所有class文件合成一个dex文件，dex文件是jar文件大小的50%左右.

2.odex（Android5.0之前）全称：Optimized DEX;即优化过的DEX.
Android5.0之前APP在安装时会进行验证和优化，为了校验代码合法性及优化代码执行速度，验证和优化后，会
产生ODEX文件，运行Apk的时候，直接加载ODEX，避免重复验证和优化，加快了Apk的响应时间.
 注意：优化会根据不同设备上Dalvik虚拟机版本、Framework库的不同等因素而不同，在一台设备上被优化过
的ODEX文件，拷贝到另一台设备上不一定能够运行。

3.oat（Android5.0之后）
 oat是ART虚拟机运行的文件,是ELF格式二进制文件,包含DEX和编译的本地机器指令,oat文件包含DEX文件，因此比ODEX文件占用空间更大。
 Android5.0以后在编译的时候(此处指系统预制app，如果通过adb install或者商店安装，在安装时
dex2oat把dex编译为odex的ELF格式文件)dex2oat默认会把classes.dex翻译成本地机器指令，生成ELF格
式的OAT文件，ART加载OAT文件后不需要经过处理就可以直接运行，它在编译时就从字节码装换成机器码了，因
此运行速度更快。不过android5.0之后oat文件还是以.odex后缀结尾,但是已经不是android5.0之前的文件
格式，而是ELF格式封装的本地机器码.
 可以认为oat在dex上加了一层壳，可以从oat里提取出dex.

4.vdex
Android8.0以后加入的,包含APK的未压缩DEX代码，另外还有一些旨在加快验证速度的元数据。

5.art (optional)
包含APK中列出的某些字符串和类的ART内部表示，用于加快应用启动速度

- AOT
ART意图了AOT(Ahread of time)技术,预先编译
在dalvik中(实际为android2.2以上引入的技术),如同其他大多数jvm一样,都采用的是jit来做及时翻译(动态翻译),将dex或odex中并排的dalvik code(或者叫smali指令集)运行态翻译成native code去执行.jit的引入使得dalvik提升了3~6倍的性能而在art中,完全抛弃了dalvik的jit,使用了aot直接在安装时用dex2oat将其完全翻译成native code.这一技术的引入,使得虚拟机执行指令的速度又一重大提升

- 提升内存利用率
在art中,它将java堆分了一块空间命名为Large-Object-Space,这块内存空间的引入用来专门存放large object.
同时art又引入了moving collector的技术,即将不连续的物理内存块进行对齐.对齐了后内存碎片化就得到了很好的解决.
Large-Object-Space的引入一是因为moving collector对大块内存的位移时间成本太高,而且提高内存的利用率
根官方统计，art的内存利用率提高10倍了左右

ART:
- 优点:
  1. 系统性能的显著提升
  2. 应用启动更快、运行更快、体验更流畅、触感反馈更及时
  3. 更长的电池续航能力
  4. 支持更低的硬件
- 缺点
  1. 更大的存储空间占用，可能会增加10%-20%
  2. 更长的应用安装时间


