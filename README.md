## Lightweight Full Text Search Server for Java

### Setup

```
Use NetBeans with [JDK8] to build or download fts.zip(WAR) from WAR Folder
Deploy to tomcat/jetty
```


![](https://github.com/iboxdb/ftserver/raw/master/FTServer/web/css/fts2.png)

### Dependencies
[iBoxDB](http://www.iboxdb.com)

[Semantic-UI](http://semantic-ui.com/)

[Jsoup](http://jsoup.org/)



### The Results Order
The results order based on the **id()** number in **class PageText**,  descending order.

Page has many PageTexts.



the Page.GetRandomContent() method is used to keep the Search-Page-Content always changing, doesn't affect the real PageText order.

if you have many pages(>100,000),  use the ID number to control the order instead of loading all pages to memory. 
Or you can load top 100-1000 pages to memory then re-order it by favor. 


#### Search Format

[Word1 Word2 Word3] => text has **Word1** and **Word2** and **Word3**

["Word1 Word2 Word3"] => text has **"Word1 Word2 Word3"** as a whole

Search [https http] => get almost all pages

#### Search Method
search (... String keywords, long **startId**, long **count**)

**startId** => which ID(the id when you created PageText) to start, 
use (startId=Long.MaxValue) to read from the top, descending order

**count** => records to read,  **important parameter**, the search speed depends on this parameter, not how big the data is.

##### Next Page
set the startId as the last id from the results of search minus one

```java
startId = search( "keywords", startId, count);
nextpage_startId = startId - 1 // this 'minus one' has done inside search()
...
//read next page
search("keywords", nextpage_startId, count)
```

mostly, the nextpage_startId is posted from client browser when user reached the end of webpage, 
and set the default nextpage_startId=Long.MaxValue, 
in javascript the big number have to write as String ("'" + nextpage_startId + "'")


#### The Page-Text and the Text-Index -Process flow

When Insert

1.insert page --> 2.insert index
````java
IndexAPI.addPage(p);
IndexAPI.addPageIndex(url);
````


When Delete

1.delete index --> 2.delete page
````java
IndexAPI.removePage(url);
````

#### Memory
````java
PageText.max_text_length
Bigger, faster, more memories.

Smaller, less memory.
````

How to set big cache
```java
//-Xmx8G
DatabaseConfig dbcfg = db.getConfig(); 
dbcfg.CacheLength = dbcfg.mb(2048);
//Or
dbcfg.CacheLength = 2048L * 1024L * 1024L;

//Wrong, overflow
//dbcfg.CacheLength = 2048 * 1024 * 1024;
```


#### Private Server
Open 
```java
public Page Html.get(String url);
```
Set your private WebSite text
```java
Page page = new Page();
page.url = url;
page.html = doc.html();
page.text = replace(doc.body().text());
return page;
```

#### More
[C# ASP.NET Core Version](https://github.com/iboxdb/ftserver-cs)



