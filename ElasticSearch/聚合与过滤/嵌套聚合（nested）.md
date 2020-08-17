## 嵌套聚合

在查询的时候，我们使用 nested 查询 就可以获取嵌套对象的信息。同理， nested 聚合允许我们对嵌套对象里的字段进行聚合操作。

```
GET /my_index/blogpost/_search
{
  "size" : 0,
  "aggs": {
    "comments": { 
      "nested": {
        "path": "comments"
      },
      "aggs": {
        "by_month": {
          "date_histogram": { 
            "field":    "comments.date",
            "interval": "month",
            "format":   "yyyy-MM"
          },
          "aggs": {
            "avg_stars": {
              "avg": { 
                "field": "comments.stars"
              }
            }
          }
        }
      }
    }
  }
}
```

nested 聚合 “进入” 嵌套的 comments 对象。  
comment对象根据 comments.date 字段的月份值被分到不同的桶。  
计算每个桶内star的平均数量。  
从下面的结果可以看出聚合是在嵌套文档层面进行的：

```
"aggregations": {
  "comments": {
     "doc_count": 4, 
     "by_month": {
        "buckets": [
           {
              "key_as_string": "2014-09",
              "key": 1409529600000,
              "doc_count": 1, 
              "avg_stars": {
                 "value": 4
              }
           },
           {
              "key_as_string": "2014-10",
              "key": 1412121600000,
              "doc_count": 3, 
              "avg_stars": {
                 "value": 2.6666666666666665
              }
           }
        ]
     }
  }
}
```

总共有4个 comments 对象 ：1个对象在9月的桶里，3个对象在10月的桶里。 

## 逆向嵌套聚合  
nested 聚合 只能对嵌套文档的字段进行操作。 根文档或者其他嵌套文档的字段对它是不可见的。 然而，通过 reverse_nested 聚合，我们可以 走出 嵌套层级，回到父级文档进行操作。  
例如，我们要基于评论者的年龄找出评论者感兴趣 tags 的分布。 comment.age 是一个嵌套字段，但 tags 在根文档中：  

```
GET /my_index/blogpost/_search
{
  "size" : 0,
  "aggs": {
    "comments": {
      "nested": { 
        "path": "comments"
      },
      "aggs": {
        "age_group": {
          "histogram": { 
            "field":    "comments.age",
            "interval": 10
          },
          "aggs": {
            "blogposts": {
              "reverse_nested": {}, 
              "aggs": {
                "tags": {
                  "terms": { 
                    "field": "tags"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

nested 聚合进入 comments 对象。  
histogram 聚合基于 comments.age 做分组，每10年一个分组。  
reverse_nested 聚合退回根文档。  
terms 聚合计算每个分组年龄段的评论者最常用的标签词。  
简略结果如下所示：

```
"aggregations": {
  "comments": {
     "doc_count": 4, 
     "age_group": {
        "buckets": [
           {
              "key": 20, 
              "doc_count": 2, 
              "blogposts": {
                 "doc_count": 2, 
                 "tags": {
                    "doc_count_error_upper_bound": 0,
                    "buckets": [ 
                       { "key": "shares",   "doc_count": 2 },
                       { "key": "cash",     "doc_count": 1 },
                       { "key": "equities", "doc_count": 1 }
                    ]
                 }
              }
           },
```

一共有4条评论。  
在20岁到30岁之间总共有两条评论。  
这些评论包含在两篇博客文章中。  
在这些博客文章中最热门的标签是 shares、 cash、equities。  

## 嵌套对象的使用时机

嵌套对象 在只有一个主要实体时非常有用，这个主要实体包含有限个紧密关联但又不是很重要的实体，例如我们的 blogpost 对象包含评论对象。 在基于评论的内容查找博客文章时， nested 查询有很大的用处，并且可以提供更快的查询效率。  
嵌套模型的缺点如下：  
+ 当对嵌套文档做增加、修改或者删除时，整个文档都要重新被索引。嵌套文档越多，这带来的成本就越大。  
+ 查询结果返回的是整个文档，而不仅仅是匹配的嵌套文档。尽管目前有计划支持只返回根文档中最佳匹配的嵌套文档，但目前还不支持。  
有时你需要在主文档和其关联实体之间做一个完整的隔离设计。这个隔离是由 父子关联 提供的。  
