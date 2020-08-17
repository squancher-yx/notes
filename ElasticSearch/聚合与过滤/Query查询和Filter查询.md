 Elasticsearch 的版本为7.3。

# 一、概念
## 1、概念

一个查询语句究竟具有什么样的行为和得到什么结果，主要取决于它到底是处Query还是Filter。两者有很大区别，我们来看下：  
Query context 查询上下文 这种语句在执行时既要计算文档是否匹配，还要计算文档相对于其他文档的匹配度有多高，匹配度越高，_score 分数就越高  
Filter context 过滤上下文 过滤上下文中的语句在执行时只关心文档是否和查询匹配，不会计算匹配度，也就是得分。  
看下官方的例子

```
GET /_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title":   "Search"        }},
        { "match": { "content": "Elasticsearch" }}
      ],
      "filter": [
        { "term":  { "status": "published" }},
        { "range": { "publish_date": { "gte": "2015-01-01" }}}
      ]
    }
  }
}
```

对上面的例子分析下：  
1. query 参数表示整个语句是处于 query context 中  
2. bool 和 match 语句被用在 query context 中，也就是说它们会计算每个文档的匹配度（_score)  
3. filter 参数则表示这个子查询处于 filter context 中  
4. filter 语句中的 term 和 range 语句用在 filter context 中，它们只起到过滤的作用，并不会计算文档的得分。  

## 2、查询数据准备

1. 创建索引

```
PUT student
{
  "settings":{
    "number_of_shards":1,
    "number_of_replicas":1
  },
  "mappings":{
      "properties":{
        "name":{"type":"text"},
        "address":{"type":"keyword"},
        "age":{"type":"integer"},
        "interests":{"type":"text"},
        "birthday":{"type":"date"}
    }
  }
}
```

2. 添加测试数据

```
POST /student/_doc/1
{
  "name":"徐小小",
  "address":"杭州",
  "age":3,
  "interests":"唱歌 画画  跳舞",
  "birthday":"2017-06-19"
}
POST /student/_doc/2
{
  "name":"刘德华",
  "address":"香港",
  "age":28,
  "interests":"演戏 旅游",
  "birthday":"1980-06-19"
}
POST /student/_doc/3
{
  "name":"张小斐",
  "address":"北京",
  "age":28,
  "interests":"小品 旅游",
  "birthday":"1990-06-19"
}
POST /student/_doc/4
{
  "name":"王小宝",
  "address":"德州",
  "age":63,
  "interests":"演戏 小品 打牌",
  "birthday":"1956-06-19"
}
POST /student/_doc/5
{
  "name":"向华强",
  "address":"香港",
  "age":31,
  "interests":"演戏 主持",
  "birthday":"1958-06-19"
}
```

看是否成功

```
GET _cat/count/student?v
```

可以看出索引已经存在，并且下面有5条数据。
 
# 二、Query查询

## 1、match查询

match query: 知道分词器的存在，会对filed进行分词操作，然后再查询 
match_all: 查询所有文档  
multi_match: 可以指定多个字段  
match_phrase: 短语匹配查询，ElasticSearch引擎首先分析（analyze）查询字符串，从分析后的文本中构建短语查询，这意味着必须匹配短语中的所有分词，并且保证各个分词的相对位置不变  

1. 查询年龄为3的（命中：ID = 1)

```
GET student/_search
{
"query":{
   "match":{"age": 3}
   }
}
```

2. 查询兴趣里包含'演戏'的 （命中 ID = 2,5,4)

```
GET student/_search
{
  "query":{
    "match":{"interests": "演戏"}
  }
}
```

这里只要interests包含'演戏','演','戏'的都会命中

3. 查询索引所有文档 (命中 ID = 1,2,3,4,5)

```
GET student/_search
{
  "query":{
    "match_all": {}
  }
}
```

4. 查询name和address包含'德' (命中 ID = 2)

```
GET student/_search
{
  "query":{
    "multi_match": {
      "query": "德",
      "fields":["name","address"]
    }
  }
}
```

说明 这里文档ID为4的address为'德州'，应该也包含'德'，但却没有被命中，原因是我们索引结构中，address属性是一个keyword类型，它是需要完全匹配，而不是包含的关系。  
如果这里query为'德州'就可以命中2条数据。  

5. 查询兴趣里包含'演员'的 （命中 无)

```
GET student/_search
{
  "query":{
    "match_phrase":{"interests": "演员"}
  }
}
```

这里和match的区别是这里是真正包含'演员'，而不是只要满足其中一个字就会被模糊命中  
重点 通过上面的例子有两点比较重要  
+ 文档字段属性如果是一个keyword类型，那就需要完全匹配才能命中。好比这个字段值是12345，那么你不论是1234还是123456都不会命中。  
+ 如果是match_phrase,那就是真正的包含关系。好比这个字段值是12345，那么你是1234就会命中，而123456不会命中。因为12345包含1234而不包含123456。  

## 2、term查询和terms查询

term query: 会去倒排索引中寻找确切的term，它并不知道分词器的存在。这种查询适合keyword 、numeric、date。  
term:查询某个字段为该关键词的文档（它是相等关系而不是包含关系）  
terms:查询某个字段里含有多个关键词的文档  

+ 查询地址等于'香港'的文档 （命中：ID = 2,5）

```
GET student/_search
{
  "query":{
    "term":{ "address":"香港"}
  }
}
```

如果仅检索'香'那是无法命中的，因为keyword需要完全匹配才能命中  

+ 查询地址等于"香港"或"北京"的 (命中: ID =2,3,5)

```
GET student/_search
{
  "query":{
    "terms":{
      "address":["香港","北京"]
    }
  }
}
```

## 3、控制查询返回的数量

返回前两条数据 （命中: ID = 2,5)

```
GET student/_search
{
  "from":0,
  "size":2,
  "query":{
    "match":{"interests": "演戏"}
  }
}
```

## 4、指定返回的字段

```
GET student/_search
{
  "_source":["name","age"],
  "query":{
    "match":{"interests": "演戏"}
  }
}
```

## 5、显示要的字段、去除不需要的字段、可以使用通配符*

```
GET student/_search
{
  "query":{
    "match_all": {}
  },
  "_source":{
     "includes": "addr*",
     "excludes": ["name","bir*"]
  }
}
```

## 6、排序

```
GET student/_search
{
  "query":{
    "match_all": {}
  },
 "sort":[{
        "age":{"order": "desc"}
      }]
}
```

## 7、范围查询

range: 实现范围查询  
include_lower: 是否包含范围的左边界，默认是true  
include_upper: 是否包含范围的右边界，默认是true  
+ 查询生日的范围 （命中 ID = 2,4,5)  

```
GET student/_search
{
    "query": {
        "range": {
            "birthday": {
                "from": "1950-01-11",
                "to": "1990-01-11",
                 "include_lower": true,
                "include_upper": false
            }
        }
    }
}
```

+ 查询年纪18到28 （命中 ID = 2,3)

```
GET student/_search
{
    "query": {
        "range": {
            "age": {
                "from": 18,
                "to": 28,
                "include_lower": true,
                "include_upper": true
            }
        }
    }
}
```

## 8、wildcard查询

允许使用通配符* 和 ?来进行查询  
\* 代表0个或多个字符  
? 代表任意一个字符  

+ 查询姓名'徐'开头的 （命中 ID = 1)

```
GET student/_search
{
    "query": {
        "wildcard": {
             "name": "徐*"
        }
    }
}
```

+ 查不到数据 

```
GET student/_search
{
    "query": {
        "wildcard": {
             "name": "徐小?"
        }
    }
}
```

疑惑：按照正常我觉得这里是可以查到数据的，因为有个name为'徐小小'可以匹配，估计是因为是中文的原因，所以没有匹配到

## 9、fuzzy实现模糊查询

模糊查询可以在Match和 Multi-Match查询中使用以便解决拼写的错误，模糊度是基于Levenshteindistance计算与原单词的距离。使用如下：   
（命中： ID = 2,5,4)

```
GET student/_search
{
    "query": {
        "fuzzy": {
            "interests": {
                "value": "演" 
            }
        }
    }
}
```

疑惑 :如果我把'演'改成'演员'就查不到数据了  
有关fuzzy描述可以参考一篇文章：Elasticsearch的误拼写时的fuzzy模糊搜索技术  

## 10、高亮搜索结果

```
{
    "query":{
        "match":{
            "interests": "演戏"
        }
    },
    "highlight": {
        "fields": {
             "interests": {}
        }
    }
}
```

# 三、Filter查询

filter是不计算相关性的，同时可以cache。因此，filter速度要快于query。  

+ 获取年龄为3的 （命中 ID = 1）

```
GET student/_search
{ 
  "post_filter":{
    "term":{"age": 3}
  }
}
```

+ 查询年纪为3或者63的 （命中 ID = 1,4)

```
GET student/_search
{ 
  "post_filter":{
    "terms":{"age":[3,63]}
  }
}
```
