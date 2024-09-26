// Toast notification system
(function() {
    // Create toast container
    const toastContainer = document.createElement('div');
    toastContainer.id = 'toastContainer';
   /* toastContainer.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
    `;*/
    toastContainer.style.cssText = `
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 9999;
        display: flex;
        flex-direction: column;
        align-items: center;
    `;
    document.body.appendChild(toastContainer);

    // Toast function
    window.showToast = function(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.style.cssText = `
            background-color: #333;
            color: #fff;
            padding: 12px 20px;
            border-radius: 4px;
            margin-bottom: 10px;
            opacity: 0;
            transition: opacity 0.4s ease-out;
            display: flex;
            justify-content: space-between;
            align-items: center;
        `;

        // Set background color based on type
        switch(type) {
            case 'success':
                toast.style.backgroundColor = '#4CAF50';
                break;
            case 'error':
                toast.style.backgroundColor = '#F44336';
                break;
            case 'warning':
                toast.style.backgroundColor = '#FFC107';
                toast.style.color = '#333';
                break;
        }

        const messageSpan = document.createElement('span');
        messageSpan.textContent = message;
        toast.appendChild(messageSpan);

        const closeButton = document.createElement('button');
        closeButton.textContent = '×';
        closeButton.style.cssText = `
            background: none;
            border: none;
            color: inherit;
            font-size: 16px;
            cursor: pointer;
            margin-left: 10px;
        `;
        closeButton.onclick = function() {
            toast.style.opacity = '0';
            setTimeout(() => toastContainer.removeChild(toast), 400);
        };
        toast.appendChild(closeButton);

        toastContainer.appendChild(toast);

        // Trigger reflow to enable transition
        toast.offsetHeight;
        toast.style.opacity = '1';

        // Auto remove toast after duration
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toastContainer.removeChild(toast), 400);
        }, duration);
    };
})();