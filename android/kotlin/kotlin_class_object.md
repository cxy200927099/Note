[TOC]

# classes and Object
```kotlin
// simple, 没有主构造函数，有默认无参的构造函数
class Invoice { /*...*/}
// empty class,可以省略花括号
class empty
// 构造函数，kotlin的class可以有一个主构造函数和多个次构造函数，主构造函数是类标头的一部分(及在类{}外面)
class Person constructor(fitstName: String) {}
// 如果主构造函数没有任何注解，则关键字 constructor 可以省略
class Person(fitstName: String) {}
// 主构造器有 注解，则必须加上关键字 constructor
class Customer public @Inject constructor(name: String) { /*...*/ }
// 主构造函数不能包含任何代码，如果想要初始化，可以在关键字 `init`代码块中执行， init代码块先于构造函数执行
class InitOrderDemo(name: String) {
    val firstProperty = "First property: $name".also(::println)
    init {
        println("First initializer block that prints ${name}")
    }

    val secondProperty = "Second property: ${name.length}".also(::println)
    init {
        println("Second initializer block that prints ${name.length}")
    }
}
// 对于主构造器中的参数初始化，kotlin实现的语法很简洁，用val或者var修饰即可，即property初始化
class Person(val firstName: String, val lastName: String, var age: Int) { /*...*/ }

// 次构造函数，用constructor修饰
class Person {
  var children: MutableList<Person> = mutableListOf<Person>()
  constructor(name: String, parent: Person){
    parent.children.add(this)
  }
}
// 主构造函数中带参数初始化时，如果有次构造函数，则必须直接或间接的委托主构造函数初始化
class Person(val name: String) {
    var children: MutableList<Person> = mutableListOf<Person>();
    constructor(name: String, parent: Person) : this(name) {
        parent.children.add(this)
    }
}
// 此类不能实例化
class DontCreateMe private constructor () { /*...*/ }

// 如果主构造函数参数有默认值，编译器在编译的时候会为其生成默认无参构造函数
class Customer(val customerName: String = "")

```
### 类实例化
```kotlin
// kotlin 没有new关键字，直接类名()即可
val invoice = Invoice()

val customer = Customer("Joe Smith")
```
### 继承
kotlin中所有类都继承了 `Any`,这个超类 有三个函数
```kotlin
public open class Any {
    public open operator fun equals(other: Any?): Boolean

    public open fun hashCode(): Int

    public open fun toString(): String
}

// 默认情况下 class 是final修饰的，即不可被继承，可以使用 `open` 关键字修饰，使其可以被继承
open class Base(p:Int)

class Derived(p:Int) : Base(p) //这里写法有点和c++类似

// 继承的类没有主构造函数，则其他的次构造函数必须 用 `super`关键字初始化父类
class MyView : View {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
}

```

### 重写
```kotlin
// 重写父类的方法
open class Shape {
    // 默认方法也是final的，不能被重写，需要 open 修饰方可重写
    open fun draw() { println("Shape draw")}
    // 不可被子类重写
    fun fill() { println("shape fill")}
}

class Circle() : Shape() {
    // 被override修饰之后，其属性变为 open了，可以被子类继续重写，如果不想则使用final修饰
    override fun draw() { println("Circle draw")}
}

var circle = Circle()
circle.draw()
circle.fill()
// 输出
// Circle draw
// shape fill


// 重写 属性，val可以被重写为var，反之亦然
interface Shape {
    val vertexCount: Int
}
// 在主构造函数中用override关键字重写属性
class Rectangle(override val vertexCount: Int = 4) : Shape // Always has 4 vertices
// 属性重写
class Polygon : Shape {
    // 这里用var重写了父类的val，因此可以随意设置其值
    override var vertexCount: Int = 0  // Can be set to any number later
}

```

### 初始化顺序

init > 次构造函数
```
class Constructors {
    init {
        println("init block")
    }
    constructor(i: Int){
        println("second constructor")
    }
}

var constructors = Constructors(3)
//输出结果
// init block
// second constructor
```

当创建一个子类实例时，父类先初始化，然后才到子类初始化
```kotlin
class Derived(
        name: String,
        val lastName: String
) : Base(name.capitalize().also { println("Argument for Base: $it") }) {

    init { println("Initializing Derived") }

    override val size: Int =
            (super.size + lastName.length).also { println("Initializing size in Derived: $it") }
}
```
运行结果
```
Argument for Base: Xingyi
Initializing Base
Initializing size in Base: 6
Initializing Derived
Initializing size in Derived: 10
```
可以看到 是所有父类都初始化完了，才到子类初始化


### 内部类调用基类的方法使用
`super@<外部类名>.method`
```kotlin
class FilledRectangle: Rectangle() {
    fun draw() { /* ... */ }
    val borderColor: String get() = "black"

    //内部类
    inner class Filler {
        fun fill() { /* ... */ }
        fun drawAndFill() {
            super@FilledRectangle.draw() // Calls Rectangle's implementation of draw()
            fill()
            println("Drawn a filled rectangle with color ${super@FilledRectangle.borderColor}") // Uses Rectangle's implementation of borderColor's get()
        }
    }
}
```

### 重写规则
```
open class Rectangle {
    open fun draw() { /* ... */ }
}

interface Polygon {
    fun draw() { /* ... */ } // interface members are 'open' by default
}
// 子类继承了Rectangle，并且实现了Polygon的接口(这个方法必须重写)
class Square() : Rectangle(), Polygon {
    // 实现了接口，必须重写draw方法
    override fun draw() {
        super<Rectangle>.draw() // call to Rectangle.draw()
        super<Polygon>.draw() // call to Polygon.draw()
    }
}
```

### 接口 Interface
Interface关键字修饰
```kotlin
package helloworld.`interface`


interface MyInterface {
    val prop: Int // abstract

    val propertyWithImplementation: String
        get() = "foo"

    fun foo() {
        println(prop)
    }
}

interface MyInterface1 {

    val propWithImplenmentation: String
        get() = "prop"
}

interface EmpterInterface{}

class Child : MyInterface {
    override val prop: Int = 29
}

fun main(args: Array<String>) {

    //var myInterface1 =  MyInterface1()  //报错,interface没有构造函数，不能实例化
    var child = Child()
    println(child.prop)
    println(child.foo())
}
```
结果输出
29
29
kotlin.Unit // 这里因为将foo函数的返回值打印，而kotlin中没有返回值的都是Unit

### 抽象类
抽象类的class和 member都要用 `abstract` 来修饰，方法没有实体，默认是 open的
```kotlin
open class Polygon {
    open fun draw() {}
}

abstract class Rectangle : Polygon() {
    abstract override fun draw()
}
```

### 伴生对象 Objects

#### object 表达式
kotlin的新奇用法，可以给函数赋值，感觉这样把函数当做引用？
```kotlin
class C {
    // Private function, so the return type is the anonymous object type
    private fun foo() = object {
        val x: String = "x"
    }

    // Public function, so the return type is Any
    fun publicFoo() = object {
        val x: String = "x"
    }

    fun bar() {
        val x1 = foo().x        // Works
        val x2 = publicFoo().x  // ERROR: Unresolved reference 'x'
    }
}
```
对象表达式访问闭包中的变量
```kotlin
fun countClicks(window: JComponent) {
    var clickCount = 0
    var enterCount = 0

    window.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            clickCount++
        }

        override fun mouseEntered(e: MouseEvent) {
            enterCount++
        }
    })
    // ...
}
```



#### object实现单例
java中实现，双重校验方式
```java
class Singleton{
  private Singleton(){}

  private volatile static Singleton singleton;

  public static Singleton getInstance(){
    if (singleton == null){
      synchronized(Singleton.class){
        if (singleton == null){
          singleton = new Singleton()
        }
      }
    }

    return singleton;
  }
}
```
kotlin的实现方式
```kotlin
/**
显式声明构造方法为private
companion object用来在class内部声明一个对象
PayServiceManager的实例instance 通过lazy来实现懒汉式加载
lazy默认情况下是线程安全的，这就可以避免多个线程同时访问生成多个实例的问题
*/
class Singleton private constructor(){
  companion object{
    @volatile
    val instance: Singleton by lazy{
      Singleton()
    }
  }
}
```

例子:
```kotlin
class Singleton private constructor(){

    init {
        println("singleton init")
    }

    companion object {
        val instance: Singleton by lazy {
            Singleton()
        }
    }

    fun sayHello(){ println("hello")}
}

val singleton = Singleton.instance
val singleton1 = Singleton.instance
singleton.sayHello()
// 输出
singleton init
hello

```

## dataclass
专门用于存放数据的类
```
// 属性在构造函数初始化
data class User(val name: String = "", val age: Int = 0)

// 部分属性在类中初始化
data class Person(val name: String) {
    var age: Int = 0
}
val person1 = Person("John")
val person2 = Person("John")
person1.age = 10
person2.age = 20
```

其他用法
```kotlin
val jane = User("Jane", 35)
val (name, age) = jane
println("$name, $age years of age") // prints "Jane, 35 years of age"
```

### copy
data修饰的类，默认实现了copy函数，其实现方式如下
```
fun copy(name: String = this.name, age: Int = this.age) = User(name, age)
```
使用方法
```kotlin
data class User(val name: String = "", val age: Int = 0)

fun main(args: Array<String>) {

    val jack = User(name = "Jack", age = 1)
    val olderJack = jack.copy(age = 2)
    println(jack)
    println(olderJack)
}
//运行结果
//User(name=Jack, age=1)
//User(name=Jack, age=2)
```


## Sealed Classed
被sealed修饰的类是抽象类，不能被实例化
```kotlin
sealed class Expr
```
