## android基础



## RecycleView缓存机制
[原文参考](https://www.wanandroid.com/wenda/show/14222)
  https://www.jianshu.com/p/3e9aa4bdaefd
- RecycleView有几级缓存
  一级缓存：mAttachedScrap 和 mChangedScrap
  二级缓存：mCachedViews
  三级缓存：ViewCacheExtension
  四级缓存：RecycledViewPool

- 每一级缓存具体作用是什么？分别在什么场景下会用到哪些缓存呢？
  mAttachedScrap：LayoutManager每次layout子View之前，那些已经添加到RecyclerView中的Item以及被删除的Item的临时存放地。使用场景就是RecyclerView滚动时、还有在可见范围内删除Item后用notifyItemRemoved方法通知更新时；

  mChangedScrap：作用：存放可见范围内有更新的Item。使用场景：可见范围内的Item有更新，并且使用notifyItemChanged方法通知更新时；

  mCachedViews：作用：存放滚动过程中没有被重新使用且状态无变化的那些旧Item。场景：滚动，prefetch；

  RecycledViewPool：作用：缓存Item的最终站，用于保存那些Removed、Changed、以及mCachedViews满了之后更旧的Item。场景：Item被移除、Item有更新、滚动过程；

写到这里发现漏讲了一个prefetch，好吧，这个prefetch机制就是RecyclerView在滚动和惯性滚动的时候，借助Handler来事先从RecycledViewPool中取出即将要显示的Item，随即扔到mCachedViews中，这样的话，当layout到这个Item时，就能直接拿来用而不用绑定数据了






