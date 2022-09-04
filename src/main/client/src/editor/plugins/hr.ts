// @ts-nocheck
export default {
  name: '_hr',
  title: '실선',
  display: 'submenu',
  innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 15.74 2.24"><g><path d="M20.15,12.75V10.51H4.41v2.24H20.15Z" transform="translate(-4.41 -10.51)"></path></g></svg>',

  add: function (core, targetElement) {
    core.context._hr = {
        currentHR: null,
    };

    let listDiv = this.setSubmenu(core);
    listDiv.querySelector('ul').addEventListener('click', this.pickHr.bind(core));
    core.initMenuTarget(this.name, targetElement, listDiv);
    listDiv = null;
  },

  setSubmenu: function (core) {
    const listDiv = core.util.createElement('div');
    const items = [
      { name: '얇은 실선', class: 'hr-thin' },
      { name: '실선', class: 'hr-normal' },
      { name: '두꺼운 실선', class: 'hr-thick' },
    ]

    let list = '';
    for (let i = 0, len = items.length; i < len; i++) {
      list += `<li>
        <button type="button" class="se-btn-list btn_line" data-command="_hr" data-value="${items[i].class}" title="${items[i].name}" aria-label="${items[i].name}">
        <hr${items[i].class ? ` class="${items[i].class}"` : ''}${items[i].style ? ` style="${items[i].style}"` : ''}/>
        </button>
      </li>`;
    }

    listDiv.className = 'se-submenu se-list-layer se-list-line';
    listDiv.innerHTML = `
      <div class="se-list-inner">
        <ul class="se-list-basic">
          ${list}
        </ul>
      </div>`;
    return listDiv;
  },


  active: function (element) {
    if (!element) {
      if (this.util.hasClass(this.context._hr.currentHR, 'on')) {
        this.controllersOff();
      }
    } else if (/HR/i.test(element.nodeName)) {
      this.context._hr.currentHR = element;
      if (!this.util.hasClass(element, 'on')) {
        this.util.addClass(element, 'on');
        this.controllersOn('hr', this.util.removeClass.bind(this.util, element, 'on'));
      }
      return true;
    }

    return false;
  },

  appendHr: function (hrTemp) {
    this.focus();
    return this.insertComponent(hrTemp.cloneNode(false), false, true, false);
  },

  pickHr: function (e) {
    e.preventDefault();
    e.stopPropagation();

    let target = e.target;
    let command = target.getAttribute('data-command');

    while (!command && !/UL/i.test(target.tagName)) {
      target = target.parentNode;
      command = target.getAttribute('data-command');
    }

    if (!command) return;

    const oNode = this.plugins.horizontalRule.appendHr.call(this, target.firstElementChild);
    if (oNode) {
      this.setRange(oNode, 0, oNode, 0);
      this.submenuOff();
    }
  }
};
