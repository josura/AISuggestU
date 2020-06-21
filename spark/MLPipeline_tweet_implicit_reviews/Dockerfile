#FROM bigtruedata/sbt BASED ON UBUNTU 18
ARG SCALA_VER=2.13.1
ARG SBT_VER=1.3.5
FROM hseeberger/scala-sbt:8u222_${SBT_VER}_${SCALA_VER}
ENV SPARK_VERSION=2.4.5
ENV SPARK_HOME /opt/spark

#ADD sparkdir/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz /opt

#RUN ln -s /opt/spark-${SPARK_VERSION}-bin-hadoop2.7 ${SPARK_HOME}


#COPY Collaborative_filtering/ /opt/test_textMining/
#COPY MLPipeline_clustering_e_classificazione/ /opt/test_textMining/

WORKDIR /opt/classificazione_e_clustering

CMD [ "bash" ]

#CMD ["sbt"]
