#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <time.h>

#define MAX_LINE 10000

void sendfile(FILE *fp, int sockfd, int buff_size) ;
ssize_t total=0;

int main() {
    // create the server socket
    int server_socket = socket(AF_INET, SOCK_STREAM,0);
    if (server_socket < 0) {
        perror("socket");
        exit(1);
    }

    // allow reuse of local addresses
    int on = 1;
	setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));

    // define the server address
    struct sockaddr_in server_address;
    bzero(&server_address, sizeof(server_address));
    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(6500);
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);


	/* set socket buffer size */
	printf("Enter socket buffer size (bytes): ");
	uint32_t sb_size = 0;
	scanf("%u", &sb_size);
	getchar();

    // bind
    if ((bind(server_socket, (struct sockaddr*) &server_address, sizeof(server_address))) < 0) {
        perror("bind");
        exit(1);
    }

    // listen
    if (listen(server_socket, 5) < 0) {
        perror("listen");
        exit(1);
    }

    struct sockaddr_in client_address;
    int client_address_len = sizeof(client_address);

    // accept
    int client_socket = accept(server_socket, (struct sockaddr*) &client_address, &client_address_len);
    if (client_socket < 0) {
        perror("accept");
        exit(1);
    }

    char file_name[512];
    bzero(file_name, sizeof(file_name));
    int n = read(client_socket, file_name, sizeof(file_name));
    if (n < 0) {
        perror("read");
        exit(1);
    }
    printf("[Request from client]: Download file : %s\n", file_name);

    struct timespec begin, end;
	clock_gettime(CLOCK_MONOTONIC_RAW, &begin);

    FILE *fp;
    fp = fopen(file_name, "rb");
    if (fp == NULL) {
        printf("File doesn't exist\n");
        close(server_socket);
        exit(1);
    }
    char buff[512] = "Sending....... ";
    strcat(buff, file_name);
    n = write(client_socket, buff, strlen(buff));
    if (n < 0) {
        perror("write");
        exit(1);
    }
    sendfile(fp, client_socket, sb_size);

    clock_gettime(CLOCK_MONOTONIC_RAW, &end);
	long duration = (end.tv_sec - begin.tv_sec)*1e3 + (end.tv_nsec - begin.tv_nsec)/1e6;
	fprintf(stdout, "received \'%s\' successfully in %ld mili seconds\n", file_name, duration);
    
    fclose(fp);
    close(client_socket);

    return 0;
}


void sendfile(FILE *fp, int sockfd, int buff_size) 
{
    int n; 
    char sendline[MAX_LINE] = {0}; 
    while ((n = fread(sendline, sizeof(char), buff_size, fp)) > 0) 
    {
        //printf("%s", sendline);
	    total+=n;
        if (n != buff_size && ferror(fp))
        {
            perror("Read File Error");
            exit(1);
        }
        
        if (send(sockfd, sendline, n, 0) == -1)
        {
            perror("Can't send file");
            exit(1);
        }
        memset(sendline, 0, MAX_LINE);
    }
}