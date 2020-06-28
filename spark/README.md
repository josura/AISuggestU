# spark recommendation
For the recommendation, an **ensemble** method was used, first by **clustering** a defined number of repos using the readme as a metric and [kmeans](https://spark.apache.org/docs/latest/ml-clustering.html#k-means) as the clustering method, then **classifying** using a [neural network](https://spark.apache.org/docs/latest/ml-classification-regression.html#multilayer-perceptron-classifier) or a [naive bayes](https://spark.apache.org/docs/latest/ml-classification-regression.html#naive-bayes) classifier.
Predictions are based on the labels predicted

## Creation of repos profiles
For the creation of repos profiles, text mining methods were used, for every word, [TF-IDF](https://spark.apache.org/docs/latest/ml-features.html#tf-idf) was used

### future plans
collaborative filtering, other types of recommendations methods or ideas.
