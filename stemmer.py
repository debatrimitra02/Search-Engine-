from nltk.stem import PorterStemmer
import os
import string
import re

stemmer=PorterStemmer()

num=100   # number of documents here
for i in range(1,num):
	filename="visitedPage"+str(i)+".txt"
	inputfile = open(filename, "r").read()
	os.remove(filename)
	words = inputfile.split()
	clean_words = [re.sub('[”“:-»]','', item) for item in words]
	stems = [stemmer.stem(word) for word in clean_words]
	outputfile=open(filename,"w")
	for stemmed in stems:
		outputfile.write('\n'.join(stemmed))
	print i


		
