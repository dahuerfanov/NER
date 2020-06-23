# NER
Named entity tagging system that requires minimal linguistic knowledge and can be applied to several target languages without substantial changes. The system is based on the ideas of the Brill’s tagger which makes it really simple. Using supervised machine learning, we construct a series of automatons (or transducers) in order to tag a given text. The final model is composed entirely of automatons and it requires a lineal time for tagging. It was tested with the Spanish data set provided in the CoNLL-2002 attaining an overall Fβ=1 measure of 60%. Also, we present an algorithm for the construction of the final transducer used to encode all the learned contextual rules.

The following paper describes the theoretical background of this project:

https://arxiv.org/abs/2006.11548


 
