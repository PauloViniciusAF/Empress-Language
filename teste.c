#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

int main() {
int numero = 25;
if(numero < 10) {
printf("Numero esta na faixa de 0 a 9\n");
} else {
if(numero < 20) {
printf("Numero esta na faixa de 10 a 19\n");
} else {
if(numero < 30) {
printf("Numero esta na faixa de 20 a 29\n");
} else {
printf("Numero eh maior ou igual a 30\n");
}}}    return 0;
}
