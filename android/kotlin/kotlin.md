[TOC]

# Kotlin
Kotlin 是一种在 Java 虚拟机上运行的静态类型编程语言，被称之为 Android 世界的Swift，由 JetBrains 设计开发并开源。
Kotlin 可以编译成Java字节码，也可以编译成 JavaScript，方便在没有 JVM 的设备上运行。
在Google I/O 2017中，Google 宣布 Kotlin 成为 Android 官方开发语言

## 优缺点
- 优点
  - 简洁：减少代码量
  - 安全：避免NullPointerException
  - 兼容性: 可以直接调用JVM，Android和浏览器的现有库
  - 函数式编程，语法糖
- 缺点：
  编译速度慢

## 基本语法

### 基本类型
整型:
|Type	|Size (bits)	|Min value	|Max value|
|---|---|---|---|
|Byte	|8	|-128	|127|
|Short	|16	|-32768	|32767|
|Int	|32	|-2,147,483,648 (-231)	|2,147,483,647 (231 - 1)|
|Long	|64	|-9,223,372,036,854,775,808 (-263)	|9,223,372,036,854,775,807 (263 - 1)|
浮点:
| type  | size(bits)  | 小数精度|
|---|---|---|
| Float  | 32  | 6-7|
| DOuble  |  64 | 15-16|

- 简单示例


- 下划线分割，增加可读性(1.1版本开始支持)
```kotlin
val oneMillion = 1_000_000
val creditCardNumber = 1234_5678_9012_3456L
val socialSecurityNumber = 999_99_9999L
val hexBytes = 0xFF_EC_DE_5E
val bytes = 0b11010010_01101001_10010100_10010010
```

- 数字比较
kotlin中没有基本数据类型，上面的当定义一个变量时，其实是kotlin自动封装了一个对象，这样可以保证不会出现空指针，所以比较的时候需要注意是
对象地址相同(===)还是，值相同(==),如下所示
```kotlin
val a: Int = 10000
println(a === a) // true，值相等，对象地址相等

//经过了装箱，创建了两个不同的对象
val boxedA: Int? = a
val anotherBoxedA: Int? = a

//虽然经过了装箱，但是值是相等的，都是10000
println(boxedA === anotherBoxedA) //  false，值相等，对象地址不一样
println(boxedA == anotherBoxedA) // true，值相等
```

- 类型转换
由于数字都是对象，所以需要显示类型转换
```kotlin
val a: Int? = 1 // A boxed Int (java.lang.Integer)
val b: Long? = a // implicit conversion yields a boxed Long (java.lang.Long)
print(b == a) // Surprise! This prints "false" as Long's equals() checks whether the other is Long as well

val b: Byte = 1 // OK, literals are checked statically
val i: Int = b // ERROR
```
推荐使用: 每个类型都自带以下方法
```
toByte(): Byte
toShort(): Short
toInt(): Int
toLong(): Long
toFloat(): Float
toDouble(): Double
toChar(): Char
```

- 运算符
```
+ - * /
```

位操作
```
shl(bits) – 左移位 (Java’s <<)
shr(bits) – 右移位 (Java’s >>)
ushr(bits) – 无符号右移位 (Java’s >>>)
and(bits) – 与
or(bits) – 或
xor(bits) – 异或
inv() – 非
```

范围:
```kotlin
a..b //表示[a-b]的范围,两边闭合
x in a..b
x !in a..b
for (i in 1..10){
    println("$i")
}
//结果输出1,2,3...8,9,10
println(100 in 1..10) //false
println(100 !in 1..10) //true
```

注意:
```
NaN is considered equal to itself
NaN 大于任何数字，包括正无穷
-0.0 < 0.0
```

字符
```kotlin
val c: Char = '1'
//支持字符
// \t, \b, \n, \r, \', \", \\ and \$
// 字符范围 ..
if (c !in '0'..'9')
    throw IllegalArgumentException("Out of range")
return c.toInt() - '0'.toInt() // Explicit conversions to numbers

```

Booleans
```
// 取值范围 true or false
// 内置运算符 ||或  &&与 !非
```

数组
```kotlin
//创建
val a = arrayOf(1,2,3)
a.forEach { println("a $it") }
val b = arrayOfNulls<Int>(3)
b.forEach { println("b $it") }
//运行结果
a 1
a 2
a 3
b null
b null
b null

//另一种赋值方式
// Creates an Array<String> with values ["0", "1", "4", "9", "16"]
val asc = Array(5) { i -> (i * i).toString() }
asc.forEach { println(it) }
```

string字符串
 ```kotlin
//普通定义
val str: String = "hello"
println(str)
//多行字符串，用trimMargin()来删除多余的空格,以及前缀符号,默认前缀符号是"|"
val text = """
|Tell me and I forget.
|Teach me and I remember.
|Involve me and I learn.
|(Benjamin Franklin)
""".trimMargin()
println(text)
 ```
运算结果:
```
hello
Tell me and I forget.
Teach me and I remember.
Involve me and I learn.
(Benjamin Franklin)
```
string模板
```kotlin
//string模板
val i = 10
println("i = $i") // prints "i = 10"
val s = "abc"
println("$s.length is ${s.length}") // prints "abc.length is 3"
val price = """
    ${'$'}9.99
    """
println(price)
```
结果输出
```
i = 10
abc.length is 3

        $9.99

```

### 变量



### 函数
示例：接收两个int类型参数，返回一个int结果
```kotlin
fun sum(a:int, b:int): int {
  return a + b
}
```

表达式赋值给函数，可以自动推断返回值类型
```kotlin
fun sum(a:int, b:int) = a + b
```

无返回值函数,返回类型`Uint`
```
fun printSum(a:int, b:int): Uint{
  println("sum of $a and $b is ${a + b}")
}
```
*注意: $a $b ${a+b} 这里用到了string模板，参考*


### 条件表达式
if
```kotlin
// Traditional usage
var max = a
if (a < b) max = b

// With else
var max: Int
if (a > b) {
    max = a
} else {
    max = b
}

// if表达式赋值
val max = if (a > b) a else b

//if块语句作为赋值，块语句最后一个表达式作为返回值
val max = if (a > b) {
    print("Choose a")
    a
} else {
    print("Choose b")
    b
}
```

when
代替了其他想c语言中的switch case表达式
```kotlin
when (x) {
    1 -> print("x == 1")
    2 -> print("x == 2")
    else -> { // Note the block
        print("x is neither 1 nor 2")
    }
}
//when分支不仅仅是常量，也可以是表达式
when (x) {
    parseInt(s) -> print("s encodes x")
    else -> print("s does not encode x")
}

when (x) {
    in 1..10 -> print("x is in the range")
    in validNumbers -> print("x is valid")
    !in 10..20 -> print("x is outside the range")
    else -> print("none of the above")
}
```

### 循环
for
```kotlin
for (i in 1..3) {
    println(i)
}
for (i in 6 downTo 0 step 2) {
    println(i)
}
for (i in array.indices) {
    println(array[i])
}
for ((index, value) in array.withIndex()) {
    println("the element at $index is $value")
}
```

while
```kotlin
while (x > 0) {
    x--
}

do {
    val y = retrieveData()
} while (y != null) // y is visible here!
```

### return,break,continue
带label的break or continue
```kotlin
loop@ for (i in 1..100) {
    for (j in 1..100) {
        if (...) break@loop
    }
}
```

return label
```kotlin
fun foo() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return // non-local return directly to the caller of foo()
        print(it)
    }
    println("this point is unreachable") //不会执行到这里
}

fun foo() {
    listOf(1, 2, 3, 4, 5).forEach lit@{
        if (it == 3) return@lit // local return to the caller of the lambda, i.e. the forEach loop
        print(it)
    }
    print(" done with explicit label")
}
// done with explicit label 打印出来了

//隐式label，使用函数名称forEach作为label
fun foo() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return@forEach // local return to the caller of the lambda, i.e. the forEach loop
        print(it)
    }
    print(" done with implicit label")
}

//返回匿名函数处
fun foo() {
    listOf(1, 2, 3, 4, 5).forEach(fun(value: Int) {
        if (value == 3) return  // local return to the caller of the anonymous fun, i.e. the forEach loop
        print(value)
    })
    print(" done with anonymous function")
}
// 输出 1245 done with anonymous function

//foo直接返回
fun foo() {
    listOf(1, 2, 3, 4, 5).forEach{
        if (it == 3) return  // local return to the caller of the anonymous fun, i.e. the forEach loop
        print(it)
    }
    print(" done with anonymous function")
}
// 输出 12


fun foo() {
    run loop@{
        listOf(1, 2, 3, 4, 5).forEach {
            if (it == 3) return@loop // non-local return from the lambda passed to run
            print(it)
        }
    }
    print(" done with nested loop")
}

//表示“在标签@a处返回1”，而不是“在标签上返回带标签的表达式（@a 1）”
return@a 1
```

## property
kotlin中的属性，有点类似c#， 只需定义好属性，默认会自动添加set，get函数
```kotlin
class Address {
    var name: String = "Holmes, Sherlock"
    var street: String = "Baker"
    var city: String = "London"
    var state: String? = null
    var zip: String = "123456"
}

class Person(val name: String?) {

    //val age: Int? //必须初始化，否则会报错，或者像name一样放到构造函数中

    var stringRepresentation: String
        get() = this.toString()
        set(value) {
            //setDataFromString(value) // parses the string and assigns values to other properties
        }
}
```

## 可见性
即
```
public private protected internal
```
的修饰,函数，属性和类，对象和接口可以在“顶层”上声明，即直接在包内声明
- 默认不加任何修饰是public的，这里不同于java，java默认是protected
- private 修饰后，只能在文件中可见
- internal 修饰，同一模块(包)中都能访问
- protected 不能用于顶级声明

### class的修饰
- private 只能在class内部访问
- protected 除了private范围外，还能在子类中访问
- internal 同一个模块中
- public 都能可见

```kotlin
//文件 hello.a
open class Outer {
    private val a = 1
    protected open val b = 2
    internal val c = 3
    val d = 4  // public by default

    protected class Nested {
        public val e: Int = 5
    }
}
//文件 hello.b
class Subclass : Outer() {
    // a is not visible
    // b, c and d are visible
    // Nested and e are visible

    override val b = 5   // 'b' is protected
}
//文件 world.a
class Unrelated(o: Outer) {
    // o.a, o.b are not visible
    // o.c and o.d are visible (same module)
    // Outer.Nested is not visible, and Nested::e is not visible either
}

```

## 关键字

### lateinit

### open,final
用于修饰类，属性，方法是否可以被继承，被重写，不加修饰的情况，默认是final修饰

### inner

### lazy




