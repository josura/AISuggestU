ARG SCALA_VER=2.13.1
ARG SBT_VER=1.3.5
FROM hseeberger/scala-sbt:8u222_${SBT_VER}_${SCALA_VER}

COPY API_examples/ /opt/API_github_ex/

WORKDIR /opt/API_github_ex

CMD [ "sbt", "run" ]

