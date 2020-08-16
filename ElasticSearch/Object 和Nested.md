嵌套聚合（nested）点击

nested类型是一种特殊的对象object数据类型(specialised version of the object datatype )，允许对象数组彼此独立地进行索引和查询。

**对象数组如何扁平化**

内部对象object字段的数组不能像我们所期望的那样工作。 Lucene没有内部对象的概念，所以Elasticsearch将对象层次结构扁平化为一个字段名称和值的简单列表。 例如，以下文件：

```
POST /lib/books/1
{
  "group" : "fans",
  "user" : [ 
    {
      "first" : "John",
      "last" :  "Smith"
    },
    {
      "first" : "Alice",
      "last" :  "White"
    }
  ]
}
```

说明
user字段被动态的添加为object类型的字段。  
在内部其转换成一个看起来像下面这样的文档：

```
{
  "group" :        "fans",
  "user.first" : [ "alice", "john" ],
  "user.last" :  [ "smith", "white" ]
}
```

user.first和user.last字段被扁平化为多值字段，并且alice和white之间的关联已经丢失。 本文档将错误地匹配user.first为alice和user.last为smith的查询：

**普通查询**

```
GET /lib/books/_search{
  "query": {
    "bool": {
      "must": [
        { "match": { "user.first": "Alice" }},
        { "match": { "user.last":  "Smith" }}
      ]
    }
  }
}
```

**使用nested：**

建立索引

```
PUT /lib/
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 0
  },
  "mappings": {
    "books": {
      "properties": {
        "users": {
          "type": "nested"
          
        }
      }
    }
  }
}
```

插入数据

```
POST /lib/books/1
{
  "group" : "fans",
  "user" : [ 
    {
      "first" : "John",
      "last" :  "Smith"
    },
    {
      "first" : "Alice",
      "last" :  "White"
    }
  ]
}
```

使用nested方式查询

```
GET /lib/books/_search
{
  "query": {
    "nested": {
      "path": "users",
      "query": {
        "bool": {
          "must": [
            { "match": { "users.first": "Alice" }},
            { "match": { "users.last":  "Smith" }} 
          ]
        }
      }
    }
  }
}
```
