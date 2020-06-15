FROM python:3-alpine

ENV GITHUB_TOKEN=""
ENV TZ=Europe/Rome

RUN apk add bash tzdata gcc musl-dev

RUN pip install pygithub requests nltk

RUN cp /usr/share/zoneinfo/Europe/Rome /etc/localtime &&\
    echo "Europe/Rome" > /etc/timezone &&\
    apk del tzdata

ADD entrypoint.sh /app/entrypoint.sh
ADD execute.sh /app/execute.sh
ADD data-parser.py /app/data-parser.py

ENTRYPOINT [ "/app/entrypoint.sh" ]
