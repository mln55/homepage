(() => {
  const head = document.querySelector('head');
  const body = document.querySelector('body');
  const header = document.querySelector('#header');

  // Breakpoints.
  breakpoints({
    xlarge:   [ '1281px',  '1680px' ],
    large:    [ '981px',   '1280px' ],
    medium:   [ '737px',   '980px'  ],
    small:    [ '481px',   '736px'  ],
    xsmall:   [ '361px',   '480px'  ],
    xxsmall:  [ null,      '360px'  ],
    'xlarge-to-max':    '(min-width: 1681px)',
    'small-to-xlarge':  '(min-width: 481px) and (max-width: 1680px)'
  });

  // Stops animations/transitions until the page has ...
  // ... loaded.
  window.addEventListener('load', () => {
    setTimeout(() => {
      if (body.className === 'is-preload') body.removeAttribute('class');
      else body.classList.remove('is-preload');
    }, 100);
  });

  // ... stopped resizing.
  var resizeTimeout;
  window.addEventListener('resize', () => {

    // Mark as resizing.
    body.classList.add('is-resizing');

    // Unmark after delay.
    clearTimeout(resizeTimeout);

    resizeTimeout = setTimeout(() => {
      if (body.className === 'is-resizing') body.removeAttribute('class');
      else body.classList.remove('is-resizing');
    }, 100);
  });


  // Fixes.

  // Object fit images.
  if (!browser.canUse('object-fit') || browser.name == 'safari') {
    const objects = document.querySelectorAll('.image.object');
    if (objects) {
      objects.forEach(function() {
        const img = this.querySelector('img');
        // Hide original image.
        img.style.opacity = 0;

        // Set background.
        this.style.backgroundImage = 'url("' + img.getAttribute('src') + '")';
        this.style.backgroundSize = img.style.objectFit ? img.style.objectFit : 'cover';
        this.style.backgroundPosition = img.style.objectPosition ? img.style.objectPosition : 'center';
      });
    }
  }

  const sidebar = document.querySelector('#sidebar');

  // Inactive by default on <= large.
  breakpoints.on('<=large', function() {
    sidebar.classList.add('inactive');
  });

  breakpoints.on('>large', function() {
    sidebar.classList.remove('inactive');
  });

  // Hack: Workaround for Chrome/Android scrollbar position bug.
  if (browser.os == 'android' && browser.name == 'chrome') {
    const style = document.createElement('style');
    style.innerText = '#sidebar .inner::-webkit-scrollbar { display: none; }';
    if(head) head.appendChild(style);
  }

  // Toggle.
  const toggle = document.createElement('div');
  toggle.className ='toggle fas fa-bars';

  toggle.addEventListener('click', e => {
    e.preventDefault();
    e.stopPropagation();

    // >large? Bail.
    if (breakpoints.active('>large')) return;

    sidebar.classList.toggle('inactive');
  });

  sidebar.appendChild(toggle);

  // Events.
  // Link clicks.
  sidebar.addEventListener('click', e => {
    // only A tag
    if (e.target.tagName != 'A') return;

    // >large? Bail.
    if (breakpoints.active('>large')) return;

    // Vars.
    const href = e.target.getAttribute('href');
    const target = e.target.getAttribute('target');

    // Prevent default.
    e.preventDefault();
    e.stopPropagation();

    // Check URL.
    if (!href || href == '#' || href == '') return;

    // Hide sidebar.
    sidebar.classList.add('inactive');

    // Redirect to href.
    setTimeout(function() {
      if (target == '_blank')
        window.open(href);
      else
        window.location.href = href;
    }, 500);
  });

  // Prevent certain events inside the panel from bubbling.
  ['click', 'touchend', 'touchstart', 'touchmove'].forEach(eStr => {
    sidebar.addEventListener(eStr, (e) => {
      // >large? Bail.
      if (breakpoints.active('>large')) return;

      // Prevent propagation.
      e.stopPropagation();
    });
  });

  // Hide panel on body click/tap.
  ['click', 'touchend'].forEach(eStr => {
    body.addEventListener(eStr, e => {
      // >large? Bail.
      if (breakpoints.active('>large')) return;

      // Deactivate.
      sidebar.classList.add('inactive');
    });
  });

  // hljs
  if (document.querySelectorAll('pre code').length !== 0) hljs.highlightAll();
})();
