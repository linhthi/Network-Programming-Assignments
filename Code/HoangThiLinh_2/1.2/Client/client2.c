#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <time.h>
#include <errno.h>
#include <sys/types.h>

#define MAX_LINE 10000
void writefile(FILE *fp, int sockfd);
ssize_t total = 0;

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
    printf("Enter file name you want to download\n");
    scanf("%s", file_name);
    int n = write(client_socket, file_name, strlen(file_name));
    if (n < 0) {
        perror("write");
        exit(1);
    }

    char buff[512];
    bzero(buff, sizeof(buff));
    n = read(client_socket, buff, sizeof(buff));
    if (n <= 0) {
        perror("Error with download file");
        exit;
    }
    else {
        printf("[Response from server]: %s\n", buff);
        bzero(buff, sizeof(buff));
        FILE *fp;
        fp = fopen(file_name, "wb");
        writefile(fp, client_socket);
        fprintf(stdout, "Download \'%s\' successfully\n", file_name);
        fclose(fp);
        close(client_socket);
    }

}

void writefile(FILE *fp, int sockfd) {
    ssize_t n;
    char buff[MAX_LINE] = {0};
     while ((n = recv(sockfd, buff, MAX_LINE, 0)) > 0) 
    {
	    total+=n;
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