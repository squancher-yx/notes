# 聚合概念

ElasticSearch除了致力于搜索之外，也提供了聚合实时分析数据的功能  
如果把搜索比喻为大海捞针(从海量的文档中找出符合条件的那一个)，那麽聚合就是去分析大海中的针们的特性，像是   
在大海里有多少针？  
针的平均长度是多少？  
按照针的製造商来划分，针的长度中位值是多少？  
每月加入到海中的针有多少？  
这里面有异常的针麽？  
因此透过聚合，我们可以得到一个数据的概览，聚合能做的是分析和总结全套的数据，而不是查找单个文档(这是搜索做的事)  
聚合允许我们向数据提出一些複杂的问题，虽然他的功能完全不同于搜索，但他们其实使用了相同的数据结构，这表示聚合的执行速度很快，并且就像搜索一样几乎是实时的  
并且由于聚合和搜索是使用同样的数据结构，因此聚合和搜索可以是一起执行的  
这表示我们可以在一次json请求裡，同时对相同的数据进行 搜索/过滤 + 分析，两个愿望一次满足  
聚合的两个主要的概念，分别是 桶 和 指标  

**桶(Buckets) :** 满足特定条件的文档的集合  
当聚合开始被执行，每个文档会决定符合哪个桶的条件，如果匹配到，文档将放入相应的桶并接着进行聚合操作  
像是一个员工属于男性桶或者女性桶，日期2014-10-28属于十月桶，也属于2014年桶  
桶可以被嵌套在其他桶里面  
像是北京能放在中国桶裡，而中国桶能放在亚洲桶裡  
Elasticsearch提供了很多种类型的桶，像是时间、最受欢迎的词、年龄区间、地理位置桶等等，不过他们在根本上都是通过同样的原理进行操作，也就是基于条件来划分文档，一个文档只要符合条件，就可以加入那个桶，因此一个文档可以同时加入很多桶  

**指标(Metrics) :** 对桶内的文档进行统计计算  
桶能让我们划分文档到有意义的集合， 但是最终我们需要的是对这些桶内的文档进行一些指标的计算  
指标通常是简单的数学运算(像是min、max、avg、sum），而这些是通过当前桶中的文档的值来计算的，利用指标能让你计算像平均薪资、最高出售价格、95%的查询延迟这样的数据  

## aggs 聚合的模板
当query和aggs一起存在时，会先执行query的主查询，主查询query执行完后会搜出一批结果，而这些结果才会被拿去aggs拿去做聚合  
另外要注意aggs后面会先接一层自定义的这个聚合的名字，然后才是接上要使用的聚合桶  
如果有些情况不在意查询结果是什麽，而只在意aggs的结果，可以把size设为0，如此可以让返回的hits结果集是0，加快返回的速度  
一个aggs裡可以有很多个聚合，每个聚合彼此间都是独立的，因此可以一个聚合拿来统计数量、一个聚合拿来分析数据、一个聚合拿来计算标准差...，让一次搜索就可以把想要做的事情一次做完  
像是此例就定义了3个聚合，分别是custom_name1、custom_name2、custom_name3  
aggs可以嵌套在其他的aggs裡面，而嵌套的桶能作用的文档集范围，是外层的桶所输出的结果集  

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": { ... },
    "size": 0,
    "aggs": {
        "custom_name1": {  //aggs后面接著的是一个自定义的name
            "桶": { ... }  //再来才是接桶
        },
        "custom_name2": {  //一个aggs裡可以有很多聚合
            "桶": { ... }
        },
        "custom_name3": {
            "桶": {
               .....
            },
            "aggs": {  //aggs可以嵌套在别的aggs裡面
                "in_name": { //记得使用aggs需要先自定义一个name
                    "桶": { ... } //in_name的桶作用的文档是custom_name3的桶的结果
                }
            }
        }
    }
}
```

结果

```
  {
   "hits": {
       "total": 8,
       "max_score": 0,
       "hits": [] //因为size设为0，所以没有查询结果返回
   },
   "aggregations": {
       "custom_name1": {
           ...
       },
       "custom_name2": {
           ...
       },
       "custom_name3": {
           ... ,
           "in_name": {
              ....
           }
       }
   }
  }
```

聚合中常用的桶 terms、filter、top_hits

**terms桶 : **针对某个field的值进行分组，field有几种值就分成几组

terms桶在进行分组时，会爲此field中的每种值创建一个新的桶  
要注意此 "terms桶" 和平常用在主查询query中的 "查找terms" 是不同的东西  

+ 具体实例  
  首先插入几笔数据，其中color是一个keyword类型

```
{ "color": "red" }
{ "color": "green" }
{ "color": ["red", "blue"] }
```

执行terms聚合

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": {
            "terms": {
                "field": "color" //使用color来进行分组
            }
        }
    }
}
```

结果

因为color总共有3种值，red、blue、green，所以terms桶为他们产生了3个bucket，并计算了每个bucket中符合的文档有哪些  
bucket和bucket间是独立的，也就是说一个文档可以同时符合好几个bucket，像是{"color": ["red", "blue"]}就同时符合了red和blue bucket

```
"aggregations": {
    "my_name": {
        "doc_count_error_upper_bound": 0,
        "sum_other_doc_count": 0,
        "buckets": [
            {
                "key": "blue",
                "doc_count": 1
            },
            {
                "key": "red",
                "doc_count": 2  //表示color为red的文档有2个，此例中就是 {"color": "red"} 和 {"color": ["red", "blue"]}这两个文档
            },
            {
                "key": "green",
                "doc_count": 1
            }
        ]
    }
}
```

+ 具体实例二
  将terms桶搭配度量指标(avg、min、max、sum...)一起使用  
  其实度量指标也可以看成一种"桶"，他可以和其他正常的桶们进行嵌套作用，差别只在指标关注的是这些文档中的某个数值的统计，而桶关注的是文档  
  首先准备数据，color一样为keyword类型，而price为integer类型

```
{ "color": "red", "price": 100 }
{ "color": "green", "price": 500 }
{ "color": ["red", "blue"], "price": 1000 }
```

将avg指标嵌套在terms桶裡一起使用

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": {
            "terms": {
                "field": "color"
            },
            "aggs": {  //嵌套两个指标avg、min在terms桶中
                "my_avg_price": { //my_avg_price计算每个bucket的平均price
                    "avg": {
                        "field": "price"
                    }
                },
                "my_min_price": { //my_min_price计算每个bucket中的最小price
                    "min": {
                        "field": "price"
                    }
                }
            }
        }
    }
}
```

结果

```
"aggregations": {
    "my_name": {
        "doc_count_error_upper_bound": 0,
        "sum_other_doc_count": 0,
        "buckets": [ //terms桶中的每个bucket都会计算avg和min两个指标
            {
                "key": "blue",
                "doc_count": 1,
                "my_avg_price": { //avg指标
                    "value": 1000
                },
                "my_min_price": { //min指标
                    "value": 100
                }
            },
            {
                "key": "red",
                "doc_count": 2,
                "my_avg_price": {  //avg指标计算的值，因为符合color为red的文档有两笔，所以平均price为100+1000/2 = 550
                    "value": 550
                },
                "my_min_price": {
                    "value": 100
                }
            },
            {
                "key": "green",
                "doc_count": 1,
                "my_avg_price": {
                    "value": 500
                },
                "my_min_price": {
                    "value": 500
                }
            }
        ]
    }
}
```

**filter桶:** 一个用来过滤的桶  
要注意此处的 "filter桶" 和用在主查询query的 "过滤filter" 的用法是一模一样的，都是过滤  
不过差别是 "filter桶" 会自己给创建一个新的桶，而不会像 "过滤filter" 一样依附在query下  
因为filter桶毕竟还是一个聚合桶，因此他可以和别的桶进行嵌套，但他不是依附在别的桶上  

+ 具体实例
  取得color为red或是blue的文档

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": {
            "filter": { //因为他用法跟一般的过滤filter一样，所以也能使用bool嵌套
                "bool": {
                    "must": {
                        "terms": { //注意此terms是查找terms，不是terms桶
                            "color": [ "red", "blue" ]
                        }
                    }
                }
            }
        }
    }
}
```

结果

```
"aggregations": {
    "my_name": {
        "doc_count": 2 //filter桶计算出来的文档数量
    }
}
```

+ 具体实例二
  filter桶和terms桶嵌套使用，先过滤出color为red以及blue的文档，再对这些文档进行color分组

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": { //my_name聚合
            "filter": { //filter桶
                "bool": {
                    "must": {
                        "terms": {
                            "color": [ "red", "blue" ]
                        }
                    }
                }
            },
            "aggs": {
                "my_name2": { //my_name2聚合，嵌套在my_name聚合裡
                    "terms": { //terms桶
                        "field": "color"
                    }
                }
            }
        }
    }
}
```

结果

因为terms桶嵌套在filter桶内，所以query查询出来的文档们会先经过filter桶，如果符合filter桶，才会进入到terms桶内  
此处通过filter桶的文档只有两笔，分别是{"color": "red"}以及{"color": ["red", "blue"]}，所以terms桶只会对这两笔文档做分组  
这也是为什麽terms桶裡没有出现color为green的分组，因为这个文档在filter桶就被挡下来了  

```
"aggregations": {
    "my_name": {
        "doc_count": 2, //filter桶计算的数量，通过此处的文档只有2笔
        "my_name2": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [ 
                {
                    "key": "red",
                    "doc_count": 2  //terms桶计算的数量
                },
                {
                    "key": "blue",
                    "doc_count": 1  //terms桶计算的数量
                }
            ]
        }
    }
}
```

**top_hits桶 :** 在某个桶底下找出这个桶的前几笔hits，返回的hits格式和主查询query返回的hits格式一模一样  
top_hits桶支持的参数  
from、size  
sort : 设置返回的hits的排序  
要注意，假设在主查询query裡已经对数据设置了排序sort，此sort并不会对aggs裡面的数据造成影响，也就是说主查询query查找出来的数据会先丢进aggs而非先经过sort，因此就算主查询设置了sort，也不会影响aggs数据裡的排序  
因此如果在top_hits桶裡的返回的hits数据想要排序，需要自己在top_hits桶裡设置sort  
如果没有设置sort，默认使用主查询query所查出来的_score排序  
_source : 设置返回的字段

+ 具体实例
  首先准备数据，color是keyword类型

```
{ "color": "red", "price": 100 }
{ "color": ["red", "blue"], "price": 1000 }
```

使用terms桶分组，再使用top_hits桶找出每个group裡面的price最小的前5笔hits

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": {
            "terms": {
                "field": "color"
            },
            "aggs": {
                "my_top_hits": {
                    "top_hits": {
                        "size": 5,
                        "sort": {
                            "price": "asc"
                        }
                    }
                }
            }
        }
    }
}
```

结果

```
"aggregations": {
    "my_name": {
        "doc_count_error_upper_bound": 0,
        "sum_other_doc_count": 0,
        "buckets": [
            {
                "key": "red",
                "doc_count": 2,  //terms桶计算出来的color为red的文档数
                "my_top_hits": {
                    "hits": {  //top_hits桶找出color为red的这些文档中，price从小到大排序取前5笔
                        "total": 2,
                        "max_score": null,
                        "hits": [
                            {
                                "_score": null,
                                "_source": { "color": "red", "price": 100 },
                                "sort": [ 100 ]
                            },
                            {
                                "_score": null,
                                "_source": { "color": [ "red", "blue" ], "price": 1000 },
                                "sort": [ 1000 ]
                            }
                        ]
                    }
                }
            },
            {
                "key": "blue",
                "doc_count": 1,  //terms桶计算出来的color为blue的文档数
                "my_top_hits": {
                    "hits": { //top_hits桶找出的hits
                        "total": 1,
                        "max_score": null,
                        "hits": [
                            {
                                "_source": {
                                    "color": [ "red", "blue" ], "price": 1000 },
                                "sort": [ 1000 ]
                            }
                        ]
                    }
                }
            }
        ]
    }
}
```

**多桶排序**
terms桶、histogram桶、data_histogram桶这些桶属于多值桶，也就是说他们会动态生成很多桶，对于这些生成出来的桶们，Elasticsearch默认会使用doc_value进行降序排序，也就是说哪个生成桶的doc_value文档数较多，哪个生成桶就排在前面  
如果想要改变这个生成桶与生成桶之间的排序，可以在使用terms桶、histogram桶、data_histogram桶时，使用order进行排序  
order支持的参数  
_count : 按照文档数排序  
_key : 按照每个桶的字符串值的字母顺序排序  

+ 具体实例
  准备数据，color是keyword类型

```
{ "color": "red", "price": 100 }
{ "color": ["red", "blue"], "price": 1000 }
```

使用terms桶进行分组，并且规定按照桶的字母顺序升序，因此a生成桶会排在最前面而z生成桶会排在最后面

```
GET 127.0.0.1/mytest/doc/_search
{
    "query": {
        "match_all": {}
    },
    "size": 0,
    "aggs": {
        "my_name": {
            "terms": {
                "field": "color",
                "order": {
                    "_key": "asc"
                }
            }
        }
    }
}
```

结果

```
"aggregations": {
    "my_name": {
        "doc_count_error_upper_bound": 0,
        "sum_other_doc_count": 0,
        "buckets": [
            {
                "key": "blue",
                "doc_count": 1
            },
            {
                "key": "red",
                "doc_count": 2
            }
        ]
    }
}
```
