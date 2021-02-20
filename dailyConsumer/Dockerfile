FROM golang

WORKDIR /go/src/app

RUN go get -u gopkg.in/confluentinc/confluent-kafka-go.v1/kafka

ADD dailyConsumer.go /go/src/app
RUN go build dailyConsumer.go

ENTRYPOINT [ "/go/src/app/dailyConsumer" ]
