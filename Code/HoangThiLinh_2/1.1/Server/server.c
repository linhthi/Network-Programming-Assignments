/** Server
 * @Fullname: Hoang Thi Linh
 * @Student code: 17020852
 * @Description:
 * Listen on port 6500
 * Recevied file from client upload
 * Buffer size from input
 */
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <unistd.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define MAX_LINE 4096

void writefile(int sockfd, FILE *fp);

int main() {
    // create the server socket
    int server_socket = socket(AF_INET, SOCK_STREAM,0);
    if (server_socket < 0) {
        perror("socket");
        exit(1);
    }

    // define the server address
    struct sockaddr_in server_address;
    bzero(&server_address, sizeof(server_address));
    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(6500);
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);

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
    printf("[Request from client]: upload file : %s\n", file_name);

    char buff[512] = "OK, now you can send file: ";
    strcat(buff, file_name);
    n = write(client_socket, buff, strlen(buff));
    if (n < 0) {
        perror("write");
        exit(1);
    }

    bzero(buff, sizeof(buff));
    FILE *fp;
    fp = fopen(file_name, "wb");
    if (fp == NULL) {
        perror("Can't open file\n");
        exit(1);
    }
    
    char addr[INET_ADDRSTRLEN];
    printf("Start receive file: %s from %s\n", file_name, inet_ntop(AF_INET, &client_address.sin_addr, addr, INET_ADDRSTRLEN));
    writefile(client_socket, fp);
    printf("Upload Success\n");
    
    fclose(fp);
    close(client_socket);

    return 0;
}

void writefile(int sockfd, FILE *fp) {
    ssize_t n;
    char buff[MAX_LINE] = {0};
     while ((n = recv(sockfd, buff, MAX_LINE, 0)) > 0) 
    {
        if (n == -1)
        {
            perror("Receive File Error");
            exit(1);
        }
        
        if (fwrite(buff, sizeof(char), n, fp) != n)
        {
            perror("Write File Error");
            exit(1);
        }
        memset(buff, 0, MAX_LINE);
    }
}