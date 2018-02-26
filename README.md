# vmouse

A virtual mouse device controlled by an Android app over network UDP packets.

## How to Build
1. Install kernel headers : `sudo apt-get install linux-headers-$(uname -r)`
2. Clone this repository: `git clone https://github.com/guybi99/vmouse.git`
3. Run `make`

## How to run
1. `insmod vmouse.ko`
2. connect in udp to `<my ip>:1337`