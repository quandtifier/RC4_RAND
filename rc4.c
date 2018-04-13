#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

#define BYTE_CONSTRAINT 256

// print the bits for debugging
void printbits(unsigned char *bits)
{
  int i;
  for (i = 0; i < BYTE_CONSTRAINT; i++) {
    int j;
    for (j = 0; j < 8; j++) {
     printf("%d", !!((bits[i] << j) & 0x80));
    }
  }
  printf("Line break\n\n");
}
// prints hex representation of data
void printData(unsigned char data[], int strlen)
{
  printf("\n\n");
  int i;
  for (i = 0; i < strlen; i++)
  {
    printf("%02hhx", data[i]);
  }
  printf("\n\n\n");
}

// RC4 KSA (key scheduling algorithm)
void ksa(char *k, unsigned char *s, int keylen)
{
  int i;
  for (i = 0; i < BYTE_CONSTRAINT; i++) {
    s[i] = i;
  }

  int j = 0;
  for (i = 0; i < BYTE_CONSTRAINT; i++)
  {
    j = (j + s[i] + k[i % keylen]) % BYTE_CONSTRAINT;
    unsigned char c = s[i];
    s[i] = s[j];
    s[j] = c;
  }
}


// RSA PGRA (psuedo-random generation algorithm)
void pgra(unsigned char k[], unsigned char s[], unsigned char in[], unsigned char out[], int strlen)
{
  int i,j,n;
  i=j=n=0;
  for (;n < strlen; n++)
  {
    i = (i + 1) % BYTE_CONSTRAINT;
    j = (j + s[i]) % BYTE_CONSTRAINT;
    unsigned char c = s[i];
    s[i] = s[j];
    s[j] = c;
    out[n] = s[(s[i] + s[j]) % BYTE_CONSTRAINT] ^ in[n];
  }
}
// if the user wants to run the test vector they enter 'test' as filename
void runTestVector(char vect[], int strlen, unsigned char bytes[])
{
  //printbits(vect);
  ksa(vect, bytes, strlen);
  //printbits(vect);
  printData(bytes, BYTE_CONSTRAINT);

}
int main(int argc, char *argv[])
{
  // structure for the command line args
  if( argc == 3 )
  {
     printf("The key supplied is:  %s\n\n", argv[2]);
     printf("The text supplied is:  %s\n\n", argv[1]);
  }
  else if( argc > 3 )
  {
     printf("Too many arguments supplied.\n");
     return -1;
  }
  else if(argc < 3)
  {
     printf("Two arguments expected: [<plaintext>.txt] [key]\n");
     return -1;
  }

  // the keystream byte array
  unsigned char bytes[BYTE_CONSTRAINT] = {0};


  if( strcmp(argv[1],"test") == 0)
  {
    unsigned char vector[] = {0x01,0x02,0x03,0x04,0x05};// test vector
    //printbits(key);
    runTestVector(vector, 5, bytes);// not consistent with desired output
    return 1;
  }

  // everything seems to work great from here down

  // open the plaintext file
  FILE *fptr = fopen(argv[1], "r");
  fseek(fptr, 0, SEEK_END);
  int fsize = ftell(fptr);
  fseek(fptr, 0, SEEK_SET); //rewinds

  // read plaintext and make reference
  char *pt = malloc(fsize + 1);
  fread(pt, fsize, 1, fptr);
  fclose(fptr);
  pt[fsize] = 0;



  // get the key from the commandline
  char *key = argv[2];

  // the first (encryption) run of RC4
  clock_t t;
  t = clock();//start timing
  ksa(key, bytes, strlen(key));
  unsigned char *ct = malloc(fsize + 1);
  pgra(key, bytes, pt, ct, fsize);
  ct[fsize] = 0;
  t = clock() - t;//calculate total time for RC4
  double t1Seconds = ((double)t)/CLOCKS_PER_SEC;
  printf("The original data:");
  printData(pt, fsize);
  printf("Encryption elapsed time: %f seconds\nEncrypted data:", t1Seconds );
  printData(ct, fsize);

  t = clock();//start time for decryption
  unsigned char *pt2 = malloc(fsize + 1);
  ksa(key, bytes, strlen(key));
  pgra(key, bytes, ct, pt2, fsize);
  t = clock() - t;//calculate decryption time
  double t2Seconds = ((double)t)/CLOCKS_PER_SEC;
  printf("Decryption elapsed time: %f seconds\nDecrypted data:",  t2Seconds);
  printData(pt2, fsize);
  double totalSeconds = t1Seconds + t2Seconds;
  printf("Total elapsed processing time for encryption and decryption: %f seconds\n", totalSeconds);

  return 0;
}
