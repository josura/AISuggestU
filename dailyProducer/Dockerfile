FROM golang

WORKDIR /go/src/app

RUN go get -u gopkg.in/confluentinc/confluent-kafka-go.v1/kafka

ADD dailyProducer.go /go/src/app
RUN go build dailyProducer.go

ENTRYPOINT [ "/go/src/app/dailyProducer" ]
