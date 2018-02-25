#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include <linux/input.h>
#include <linux/delay.h>
#include <linux/kthread.h>
#include <linux/slab.h>

#include <net/sock.h>


MODULE_LICENSE("GPL");
MODULE_AUTHOR("Guy B");
MODULE_DESCRIPTION("A virtual mouse device");

#define PORT 1337

struct input_dev *mouse_dev;

struct service {
	struct socket *listen_socket;
	struct task_struct *thread;
};

struct service *svc;

int recv_msg(struct socket *sock, struct sockaddr_in *cl,
		unsigned char *buf, int len) 
{
	struct msghdr msg;
	struct kvec iov;
	int size = 0;

	iov.iov_base = buf;
	iov.iov_len = len;

	msg.msg_control = NULL;
	msg.msg_controllen = 0;
	msg.msg_flags = 0;
	msg.msg_name = cl;
	msg.msg_namelen = sizeof(struct sockaddr_in);

	size = kernel_recvmsg(sock, &msg, &iov, 1, len, msg.msg_flags);

	// if (size > 0)
	// {
	// 	printk(KERN_ALERT "the message is : %s\n",buf);
	// }

	return size;
}

int send_msg(struct socket *sock, struct sockaddr_in *cl, char *buf, int len) 
{
	struct msghdr msg;
	struct kvec iov;
	int size;

	iov.iov_base = buf;
	iov.iov_len = len;

	msg.msg_control = NULL;
	msg.msg_controllen = 0;
	msg.msg_flags = 0;
	msg.msg_name = cl;
	msg.msg_namelen = sizeof(struct sockaddr_in);

	size = kernel_sendmsg(sock, &msg, &iov, 1, len);

	if (size > 0)
	{
		printk(KERN_INFO "message sent!\n");
	}

	return size;
}

static int handle_mouse_movements(unsigned char *buf, const size_t len)
{
	pr_info("got mouse request\n");

	typedef struct
	{
		char type;
		union
		{
			int rel_x;
			int rel_y;
		};

		union
		{
			int btn;
			bool on;
		};
	} __packed MouseRequest;

	MouseRequest *req = buf;

	pr_info("type %c rel_x %d rel_y %d\n", req->type, req->rel_x, req->rel_y);

	switch(req->type)
	{
	case 'M': // movement
		input_report_rel(mouse_dev, REL_X, req->rel_x);
		input_report_rel(mouse_dev, REL_X, req->rel_y);
		break;

	case 'C': // click
		// input_report_key(mouse_dev, )

	default:
		pr_warn("got unknown type: %c\n", req->type);
	}

	input_sync(mouse_dev);

	return 0;
}

int start_listen(void)
{
	int error, i, size;
	struct sockaddr_in sin, client;
	int len = 15;
	unsigned char buf[len+1];

	error = sock_create(PF_INET, SOCK_DGRAM, IPPROTO_UDP, &svc->listen_socket);
	if(error < 0)
	{
		printk(KERN_ERR "cannot create socket\n");
		return -1;
	}

	sin.sin_addr.s_addr = htonl(INADDR_ANY);
	sin.sin_family = AF_INET;
	sin.sin_port = htons(PORT);

	error = kernel_bind(svc->listen_socket, (struct sockaddr*)&sin,
			sizeof(sin));
	if(error < 0) 
	{
		printk(KERN_ERR "cannot bind socket, error code: %d\n", error);
		return -1;
	}

	i = 0;
	while (1) 
	{
		memset(&buf, 0, len+1);
		memset(&client, 0, sizeof(struct sockaddr_in));
		size = recv_msg(svc->listen_socket, &client, buf, len);
		if (size < 0) 
		{
			return -1;
		}
		handle_mouse_movements(buf, len);
		// send_msg(svc->listen_socket, &client, buf, size);
	}

	return 0;
}




static int __init vmouse_init(void)
{
	pr_info("Hello from Phone vMouse!\n");

	mouse_dev = input_allocate_device();
	if (NULL == mouse_dev)
	{
		return -ENOMEM;
	}

	mouse_dev->name = "Phone vMouse";

	input_set_capability(mouse_dev, EV_REL, REL_X);
	input_set_capability(mouse_dev, EV_REL, REL_Y);
	input_set_capability(mouse_dev, EV_KEY, BTN_LEFT);
	input_set_capability(mouse_dev, EV_KEY, BTN_RIGHT);
	input_set_capability(mouse_dev, EV_KEY, BTN_MIDDLE);

	if (input_register_device(mouse_dev))
	{
		input_free_device(mouse_dev);
		return -ENOMEM;
	}

	// TODO: remove later
	// kthread_run(do_movements_test, NULL, "movments_test");

	pr_info("init mouse udp server\n");
	svc = kmalloc(sizeof(struct service), GFP_KERNEL);
	svc->thread = kthread_run((void *)start_listen, NULL, "mouse-server");

    return 0;
}

static void __exit vmouse_cleanup(void)
{
	pr_info("killing udp server.\n");
	if (svc->listen_socket != NULL) 
	{
		svc->listen_socket->ops->release(svc->listen_socket);
		printk(KERN_ALERT "release socket\n");
	}
	kfree(svc);

	pr_info("unregistering input device\n");
	input_unregister_device(mouse_dev);
	input_free_device(mouse_dev);
}

module_init(vmouse_init);
module_exit(vmouse_cleanup);
