FROM golang

WORKDIR /go/src/app

RUN apt-get install -y git
RUN git clone --branch master https://github.com/elastic/go-elasticsearch.git $GOPATH/src/github.com/elastic/go-elasticsearch

ADD user-recomendation.go /go/src/app
RUN go build user-recomendation.go

ENTRYPOINT [ "/go/src/app/user-recomendation", "herbrant" ]