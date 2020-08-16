### 批量获取文档

使用es提供的Multi Get API： 

使用Multi Get API可以通过索引名、类型名、文档id一次得到一个文档集合，文档可以来自同一个索引库，也可以来自不同索引库

**批量获取**

```
GET /_mget
{
    "docs":[
       {
           "_index": "lib",
           "_type": "user",
           "_id": 1
       },
       {
           "_index": "lib",
           "_type": "user",
           "_id": 2
       },
       {
           "_index": "lib2",
           "_type": "user",
           "_id": 1
       }
     ]
}
```

**指定不同字段**

```
GET /_mget
{
    "docs":[
       {
           "_index": "lib",
           "_type": "user",
           "_id": 1,
           "_source": "interests"
       },
       {
           "_index": "lib2",
           "_type": "user",
           "_id": 1,
           "_source": ["age","interests"]
       }
       
     ]
}
```

**获取同索引同类型下的不同文档：**

```
GET /lib/user/_mget
{
    "docs":[
       {
           "_id": 1
       },
       {
           "_source": "interests",
           "_id": 2
       }
     ]
}

GET /lib/user/_mget
{
   "ids": ["1","2"]
}
```


**使用Bulk API 实现批量操作**

bulk的格式：

```
{action:{metadata}}\n
{requstbody}\n

```

示例：

```
{"delete":{"_index":"lib","_type":"user","_id":"1"}}
```

  action:(行为)

  create：文档不存在时创建
  
  update:更新文档
  
  index:创建新文档或替换已有文档
  
  delete:删除一个文档，删除没有请求体

  metadata：_index,_type,_id
  
create 和index的区别:
如果数据存在，使用create操作失败，会提示文档已经存在，使用index则可以成功执行。

**批量添加（PUT也行，注意格式）:**

```
POST /lib2/books/_bulk
{"index":{"_id":1}}
{"title":"Java","price":55}
{"index":{"_id":2}}
{"title":"Html5","price":45}
{"index":{"_id":3}}
{"title":"Php","price":35}
{"index":{"_id":4}}
{"title":"Python","price":50}
```

**批量获取**

```
GET /lib2/books/_mget
{"ids": ["1","2","3","4"]}
```

**把所有的操作组合在一起，一个完整的 bulk 请求 有以下形式:**

```
POST /_bulk
{ "delete": { "_index": "website", "_type": "blog", "_id": "123" }} 
{ "create": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "title":    "My first blog post" }
{ "index":  { "_index": "website", "_type": "blog" }}
{ "title":    "My second blog post" }
{ "update": { "_index": "website", "_type": "blog", "_id": "123", "_retry_on_conflict" : 3} }
{ "doc" : {"title" : "My updated blog post"} } 
```

详细点击
