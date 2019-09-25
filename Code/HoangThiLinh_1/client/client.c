/** Client
 * @Fullname: Hoang Thi Linh
 * @Student code: 17020852
 * @Description:
 * Input server's IP address and port from the keyboard.
 * Send and Recive with Server
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

int main() {

    //create the socket client
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

    // connect
    if (connect(client_socket, (struct sockaddr*) &server_address, sizeof(server_address)) < 0) {
        perror("connet");
        exit(1);
    }

    // read/write
    char buff[512] = "Hello server!";
    int nbytes = write(client_socket, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("write");
        exit;
    }

    nbytes = read(client_socket, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("read");
        exit(1);
    }
    printf("[Message from server]: %s\n", buff);

    nbytes = read(client_socket, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("read");
        exit(1);
    }
    printf("[Message from server]: %s\n", buff);

    char name[50];
    scanf("%s", name);
    strcpy(buff, "My name is ");
    strcat(buff, name);
    strcat(buff, "! Nice to meet you!");
    nbytes = write(client_socket, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("write");
        exit;
    }
    printf("[Send server message]: %s\n", buff);

    nbytes = read(client_socket, buff, sizeof(buff));
    if (nbytes < 0) {
        perror("read");
        exit(1);
    }
    printf("[Message from server]: %s\n", buff);


    close(client_socket);

    return 0;
}