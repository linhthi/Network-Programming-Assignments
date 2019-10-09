#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define MAX_LINE 4096
void sendfile(FILE *fp, int sockfd);

int main() {
    // create the client socket
    int client_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (client_socket < 0) {
        perror("socket");
        exit(1);
    }

    // specify an address for socket
    struct sockaddr_in server_address;
    bzero(&server_address, sizeof(server_address));
    server_address.sin_family = AF_INET;

    char *server_ip;
	char buf[256];
	printf("Enter server's IP addr and port in the form of <IP>:<port>\n");
	scanf("%s", buf);
	getchar();	/* remove '\n' character from the stdin */

	/* parse the buf to get ip address and port */
	server_ip = strtok(buf, ":");
	server_address.sin_addr.s_addr = inet_addr(server_ip);
	server_address.sin_port = htons(atoi(strtok(NULL, ":")));

    // connect with server
    if (connect(client_socket, (struct sockaddr*) &server_address, sizeof(server_address)) < 0) {
        perror("connect");
        exit(1);
    }

    // Enter file_name client want upload and send file name to server
    char file_name[512];
    printf("Nhap ten file muon upload\n");
    scanf("%s", file_name);
    int n = write(client_socket, file_name, strlen(file_name));
    if (n < 0) {
        perror("write");
        exit(1);
    }

    char buff[512];
    bzero(buff, sizeof(buff));
    n = read(client_socket, buff, sizeof(buff));
    printf("[Response from server]:%s\n", buff);
    bzero(buff, sizeof(buff));

    FILE *f;
    f = fopen(file_name, "rb");
    if (f == NULL) {
        perror("Can't open file\n");
        exit(1);
    }
    else {
        sendfile(f, client_socket);
        fclose(f);
        printf("Send Success\n");
        close(client_socket);
    }
}

void sendfile(FILE *fp, int sockfd) 
{
    int n; 
    char sendline[MAX_LINE] = {0}; 
    while ((n = fread(sendline, sizeof(char), MAX_LINE, fp)) > 0) 
    {
        if (n != MAX_LINE && ferror(fp))
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