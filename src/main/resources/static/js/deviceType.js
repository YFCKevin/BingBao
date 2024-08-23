function isMobile() {
  const userAgent = navigator.userAgent || navigator.vendor || window.opera;
  const isIOS = /iPad|iPhone|iPod/.test(userAgent) && !window.MSStream;
  const isAndroid = /Mobi|Android/i.test(userAgent);
  const isWindowsPhone = /Windows Phone/i.test(userAgent);
  const isWindowsTablet = /Windows/i.test(userAgent) && !/Mobile/i.test(userAgent);
  const isBlackBerry = /BlackBerry/i.test(userAgent) || /BB10/i.test(userAgent);

  return isIOS || isAndroid || isWindowsPhone || isWindowsTablet || isBlackBerry;
}

function getDeviceType() {
  const userAgent = navigator.userAgent || navigator.vendor || window.opera;
  const isIOS = /iPad|iPhone|iPod/.test(userAgent) && !window.MSStream;
  const isAndroid = /Mobi|Android/i.test(userAgent);
  const isWindowsPhone = /Windows Phone/i.test(userAgent);
  const isWindowsTablet = /Windows/i.test(userAgent) && !/Mobile/i.test(userAgent);
  const isBlackBerry = /BlackBerry/i.test(userAgent) || /BB10/i.test(userAgent);

  return isIOS ? 'iOS' :
         isAndroid ? 'Android' :
         isWindowsPhone ? 'Windows Phone' :
         isWindowsTablet ? 'Windows Tablet' :
         isBlackBerry ? 'BlackBerry' :
         'Desktop';
}

document.addEventListener('DOMContentLoaded', () => {
  const deviceType = getDeviceType();
  console.log(`Current Device Type: ${deviceType}`);

  const userAgent = navigator.userAgent.toLowerCase();
  const isLineInAppBrowser = userAgent.indexOf('line') !== -1;

  if (isLineInAppBrowser) {
    const url = window.location.href;
    window.location.href = url + (url.indexOf('?') === -1 ? '?' : '&') + 'openExternalBrowser=1';
  }
});
