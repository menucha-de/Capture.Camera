CC ?= gcc
CFLAGS=-std=c99 -Iinclude -I$(JDK_INCLUDE) -I$(JDK_INCLUDE)/linux -I$(INCLUDE) -O3 -Wall -fmessage-length=0 -fPIC -MMD -MP
INCLUDE=target/include
LDFLAGS=-shared `pkg-config --libs libv4l2`
SOURCES=src/main/native/havis_capture_adapter_camera_v4l_V4l.c
TARGET=target/classes/libv4l.so
OBJS=$(SOURCES:.c=.o)

ifeq ($(shell uname -m), armv7l)
	ARCH ?= armhf
else
	ARCH ?= amd64
endif

JDK_INCLUDE=/usr/lib/jvm/default-java/include

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(LDFLAGS) $(CFLAGS) -o $@ $(OBJS)

clean:
	rm -f $(OBJS) $(TARGET) src/*.d
