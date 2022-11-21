// @ts-nocheck
import hljs from 'highlight.js';
import 'highlight.js/styles/stackoverflow-dark.css';

hljs.configure({ignoreUnescapedHTML: true});

export default {
  name: '_code',
  title: '코드 삽입',
  display: 'dialog',
  innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512"><!-- Font Awesome Pro 5.15.4 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license (Commercial License) --><path d="M278.9 511.5l-61-17.7c-6.4-1.8-10-8.5-8.2-14.9L346.2 8.7c1.8-6.4 8.5-10 14.9-8.2l61 17.7c6.4 1.8 10 8.5 8.2 14.9L293.8 503.3c-1.9 6.4-8.5 10.1-14.9 8.2zm-114-112.2l43.5-46.4c4.6-4.9 4.3-12.7-.8-17.2L117 256l90.6-79.7c5.1-4.5 5.5-12.3.8-17.2l-43.5-46.4c-4.5-4.8-12.1-5.1-17-.5L3.8 247.2c-5.1 4.7-5.1 12.8 0 17.5l144.1 135.1c4.9 4.6 12.5 4.4 17-.5zm327.2.6l144.1-135.1c5.1-4.7 5.1-12.8 0-17.5L492.1 112.1c-4.8-4.5-12.4-4.3-17 .5L431.6 159c-4.6 4.9-4.3 12.7.8 17.2L523 256l-90.6 79.7c-5.1 4.5-5.5 12.3-.8 17.2l43.5 46.4c4.5 4.9 12.1 5.1 17 .6z"/></svg>',

  add: function (core) {
      const context = core.context;
      context._code = {
          codeContentElement: null,
          codeLanguageSelect: null,
          focusElement: null,
      };

      let codeDialog = this.setDialog(core);
      context._code.modal = codeDialog;
      context._code.codeContentElement = codeDialog.querySelector('._se_code_content');
      context._code.codeLanguageSelect = codeDialog.querySelector('._se_code_language');
      context._code.focusElement = context._code.codeContentElement;

      codeDialog.querySelector('form').addEventListener('submit', this.submit.bind(core));
      context.dialog.modal.appendChild(codeDialog);
      codeDialog = null;
  },

  setDialog: function () {
    const languages = [
      'java', 'javascript', 'typescript', 'python',
      'c', 'cpp', 'json', 'html', 'xml',
      'bash', 'sql', 'yaml', 'properties', 'kotlin'
    ];
    const dialog = document.createElement('div');
    dialog.className = 'se-dialog-content';
    dialog.style.display = 'none';

    let html = '';
    html += `<form class="editor-code-form">
      <div class="se-dialog-header">
        <button type="button" data-command="close" class="se-btn se-dialog-close fas fa-times" aria-label="Close" title="닫기"></button>
        <span class="se-modal-title">코드 삽입</span>
      </div>
      <div class="se-dialog-body">
        <div class="se-dialog-form se-dialog-form-header">
          <select class="se-input-select _se_code_language" title="언어">`;
    for (let i = 0, len = languages.length; i < len; ++i) {
      html += `<option value=${languages[i]}>${languages[i]}</option>`
    }
    html += `</select>
        </div>
        <div class="se-dialog-form se-dialog-form-footer">
          <label>내용</label>
          <textarea class="se-input-form _se_code_content"></textarea>
        </div>
      </div>
      <div class="se-dialog-footer">
        <button type="submit" class="se-btn-primary" title="확인">
          <span>확인</span>
        </button>
      </div>
    </form>
    `;

    dialog.innerHTML = html;
    return dialog;
  },

  open: function () {
      this.plugins.dialog.open.call(this, '_code', '_code' === this.currentControllerName);
  },

  submit: function (e) {

    this.showLoading();
    e.preventDefault();
    e.stopPropagation();

    const submitAction = function () {
      if (this.context._code.codeContentElement.value.trim().length === 0) return false;

      const contextCode = this.context._code;
      const content = contextCode.codeContentElement.value;
      const lang = contextCode.codeLanguageSelect.selectedOptions[0].value;

      const oPre = document.createElement('pre');
      const oSpan = document.createElement('span');
      const oCode = document.createElement('code');

      oPre.className = `code-container ${lang}`;

      oCode.textContent = content;
      oSpan.textContent = lang;
      oSpan.className = 'code-language'
      oPre.appendChild(oSpan);
      oPre.appendChild(oCode);

      hljs.highlightElement(oCode);
      this.insertNode(oPre);
    }.bind(this);

    try {
        submitAction();
    } finally {
        this.plugins.dialog.close.call(this);
        this.closeLoading();
        this.focus();
    }

    return false;
  },

  on: function (update) {
      if (!update) {
          this.plugins._code.init.call(this);
      } else if (this.context._code._linkAnchor) {
          this.context.dialog.updateModal = true;
      }
  },

  init: function () {
      const contextCode = this.context._code;
      contextCode.codeContentElement.value = '';
      contextCode.codeLanguageSelect.selectedIndex = 0;
  }
};
