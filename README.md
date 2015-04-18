# Search-Engine-
Here we create a Search Engine from scratch using JAVA implementation of Page Rank Algorithm

======Pipeline of the method==========

Crawl the Web =====>
We crawled the web in the ics.uci.edu domain and using 2 bots pinging with a politeness delay of 1000 ms. It took us around 36 hours to crawl around 4 gigs of web data. A lot of filters has been applied including avoiding calenders and istory webpages to avoid falling into a web- trap and looping for infinite.

PreProcessing ===> 
1. Cleaning the web files, removing unwanted data, images etc.
2. Stemming (Porter Stemmer)


Exploratory Analytics ====>
1. Finding unigrams, Bigrams and 3-grams.
2. Building bag of words model
3. Using Hashmap we developed a map object between words (features in the space) and the documents

Ranking ==>
1. Using TF-IDF based scores to score each document based on the search term and finding the nearest neighbors using the ranking metric. 
2. Scalable code to do the whole search in few ms.

Evaluation====> 
1. Google is considered as gold standard.
2. Google has been pinged through a code on the same search query restricting the results withing ics.uci.edu domain
3. Calculating NSDA scores and validating our search results. 

UI ===>
We finally built a simple User Interface for the search Engine where people can enter the query and get a search result. 


