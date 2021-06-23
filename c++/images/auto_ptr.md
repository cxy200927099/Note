[toc]

## 智能指针介绍

智能指针是一个RAII（Resource Acquisition is initialization）类模型，用来动态的分配内存。它提供所有普通指针提供的接口，却很少发生异常。在构造中，它分配内存，当离开作用域时，它会自动释放已分配的内存。这样的话，程序员就从手动管理动态内存的繁杂任务中解放出来了。

## auto_ptr用法及好处

C++98提供了第一种智能指针：auto_ptr
```cpp

class Test
{
    public:
    Test(int a = 0 ) : m_a(a) { }
    ~Test( )
    {
       cout << "Calling destructor" << endl;
    }
    public: int m_a;
};
void main( )
{
    std::auto_ptr<Test> p( new Test(5) );
    cout << p->m_a << endl;
}
```

上述代码会智能地释放与指针绑定的内存。作用的过程是这样的：我们申请了一块内存来放Test对象，并且把他绑定到auto_ptr p上。所以当p离开作用域时，它所指向的内存块也会被自动释放。



```cpp

//***************************************************************
class Test
{
public:
 Test(int a = 0 ) : m_a(a)
 {
 }
 ~Test( )
 {
  cout<<"Calling destructor"<<endl;
 }
public:
 int m_a;
};
//***************************************************************
void Fun( )
{
 int a = 0, b= 5, c;
 if( a ==0 )
 {
  throw "Invalid divisor";
 }
 c = b/a;
 return;
}
//***************************************************************
void main( )
{
 try
 {
  std::auto_ptr<Test> p( new Test(5) );
  Fun( );
  cout<<p->m_a<<endl;
 }
 catch(...)
 {
  cout<<"Something has gone wrong"<<endl;
 }
}

```

上面的例子中，尽管异常被抛出，但是指针仍然正确地被释放了。这是因为当异常抛出时，栈松绑（stack unwinding）,当try 块中的所有对象destroy后，p 离开了该作用域，所以它绑定的内存也就释放了。

## 缺点

### 指针所有权转移

当auto_ptr被作为函数参数传递给另一个函数时，所有权发生转移，这可能会引起程序crash，如下代码所示

在main函数中定义了auto_ptr的p，然后将其作为参数传递给Fun函数，当Fun执行完毕后，指针的所有权不会在返回给 p

```cpp

//***************************************************************
class Test
{
public:
 Test(int a = 0 ) : m_a(a)
 {
 }
 ~Test( )
 {
  cout<<"Calling destructor"<<endl;
 }
public:
 int m_a;
};


//***************************************************************
void Fun(auto_ptr<Test> p1 )
{
 cout<<p1->m_a<<endl;
}
//***************************************************************
void main( )
{
 std::auto_ptr<Test> p( new Test(5) );
 Fun(p);
 cout<<p->m_a<<endl;
}
```

由于auto_ptr的野指针行为，上面的代码导致程序崩溃。在这期间发生了这些细节，p拥有一块内存，当Fun调用时， p把关联的内存块的所有权传给了auto_ptr p1, p1是p的copy（注：这里从Fun函数的定义式看出，函数参数时值传递，所以把p的值拷进了函数中），这时p1就拥有了之前p拥有的内存块。目前为止，一切安好。现在Fun函数执行完了，p1离开了作用域，所以p1关联的内存块也就释放了。那么p呢？p什么都没了，这就是crash的原因了，下一行代码还试图访问p，好像p还拥有什么资源似的


### auto_ptr不能指向数组对象

```cpp

//***************************************************************
void main( )
{
 std::auto_ptr<Test> p(new Test[5]);
}
```

上面的代码将产生一个运行时错误。因为当auto_ptr离开作用域时，delete被默认用来释放关联的内存空间。当auto_ptr只指向一个对象时，这当然是没问题的，但是在上面的代码里，我们在堆里创建了一组对象，应该使用delete[]来释放，而不是delete.




### auto_ptr不能和标准容器（vector,list,map....)一起使用

因为auto_ptr容易产生错误，所以它也将被废弃了。C++11提供了一组新的智能指针，每一个都各有用武之地。

shared_ptr

unique_ptr

weak_ptr






























