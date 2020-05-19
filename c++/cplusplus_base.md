
# 指针相关

## [std::nothrow](http://www.cplusplus.com/reference/new/nothrow/?kw=nothrow)
是一个常量，用于操作符new和new[]，表示当申请空间失败的时候，不会抛出异常，而是返回一个null pointer

正常来说，如果使用 new和new[]是有可能失败的，这时候会抛出bad_alloc exception
- 具体使用
```cpp
// nothrow example
#include <iostream>     // std::cout
#include <new>          // std::nothrow

int main () {
  std::cout << "Attempting to allocate 1 MiB... ";
  char* p = new (std::nothrow) char [1048576];

  if (!p) {             // null pointers are implicitly converted to false
    std::cout << "Failed!\n";
  }
  else {
    std::cout << "Succeeded!\n";
    delete[] p;
  }

  return 0;
}
```

## 智能指针





