FROM  java:latest
USER root
EXPOSE 2181
EXPOSE 9999
EXPOSE 9989
ADD start.sh /

### install SSH service, still needs to be started.
RUN apt-get update && apt-get install -y openssh-server

### ssh key authentication
RUN mkdir /root/.ssh ; chown -R root:root /root/.ssh;chmod -R 700 /root/.ssh
RUN echo "StrictHostKeyChecking=no" >> /etc/ssh/ssh_config
RUN ssh-keygen -b 2048 -t rsa -f ~/.ssh/id_rsa -q -N "" && \
        cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
RUN mkdir /var/run/sshd

### Deploy accumulo and start.
CMD /start.sh

