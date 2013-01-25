CC=gfortran
SOURCES=mvndstpack.f mvnexppack.f
OBJECTS=$(SOURCES:.f=.o)
LDFLAGS=-Wall -shared
CFLAGS=-Wall -fPIC

all: mvnpack.so

mvnpack.so: $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@
	cp mvnpack.so ../resources

.f.o:
	$(CC) $(CFLAGS) -c $<

clean:
	rm -f *.o mvnpack.so
