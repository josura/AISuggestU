FROM golang

WORKDIR /go/src/app

ADD user-recommendation.go /go/src/app
RUN go build user-recommendation.go

ENTRYPOINT [ "/go/src/app/user-recommendation", "herbrant" ]