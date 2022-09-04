// @ts-nocheck
export const validPost = {
  postId: -1,
  title: '',
  categoryId: '',
  content: '',
  visible: false,
};

export const oldImages: string[] = [];

export const alertWhenLeave = e => {
  e.preventDefault()
  return e.returnValue = '';
}

export default {
  name: '_submit',
  title: '게시글 작성',
  display: 'command',
  buttonClass: '',
  innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!-- Font Awesome Pro 5.15.4 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license (Commercial License) --><path d="M296 384h-80c-13.3 0-24-10.7-24-24V192h-87.7c-17.8 0-26.7-21.5-14.1-34.1L242.3 5.7c7.5-7.5 19.8-7.5 27.3 0l152.2 152.2c12.6 12.6 3.7 34.1-14.1 34.1H320v168c0 13.3-10.7 24-24 24zm216-8v112c0 13.3-10.7 24-24 24H24c-13.3 0-24-10.7-24-24V376c0-13.3 10.7-24 24-24h136v8c0 30.9 25.1 56 56 56h80c30.9 0 56-25.1 56-56v-8h136c13.3 0 24 10.7 24 24zm-124 88c0-11-9-20-20-20s-20 9-20 20 9 20 20 20 20-9 20-20zm64 0c0-11-9-20-20-20s-20 9-20 20 9 20 20 20 20-9 20-20z"/></svg>',

  add: function(core) { // 플러그인이 처음 실행 될 때 호출
    core.context._submit = {
      invalidAction: function() {
        alert('유효하지 않은 요청입니다. 다시 시도해주세요.');
        /* TODO?
          hidden element가 없는 경우 -> page refresh
          value 등을 임의로 변경한 경우 -> 현재 작성되어 있는 정보를 바탕으로 다시 설정
        */
      }
    };
  },

  action: async function() {
    // input elements
    const titleEl = document.querySelector('input[name="title"]');
    const categoryEl = document.querySelector('input[name="category"]');
    const contentEl = document.querySelector('input[name="content"]');
    const visibleEl = document.querySelector('input[name="visible"]');
    const editorEl = document.querySelector('.sun-editor-editable');

    // null element
    if (!titleEl || !categoryEl || !contentEl || !visibleEl || !editorEl) this.context._submit.invalidAction();

    // empty title
    if (titleEl.value.trim().length === 0) {
      titleEl.focus();
      alert('제목을 입력 해주세요.');
      return;
    }

    // empty content
    if (editorEl.textContent.trim().length === 0) {
      editorEl.focus();
      alert('내용을 입력 해주세요.');
      return;
    }

    // validate information
    if (
      titleEl.getAttribute('value') !== validPost.title
      || categoryEl.getAttribute('value') != validPost.categoryId
      || contentEl.getAttribute('value') !== validPost.content
      || visibleEl.getAttribute('value') !== String(validPost.visible)
    ) this.context._submit.invalidAction();

    this.showLoading();

    /********************************************************************************
      db에 태그 형식으로 저장하기에 불필요한 속성을 제거하고
      필요한 속성을 추가한 후 요청을한다.
    ********************************************************************************/

    // H3, H4 태그 북마크를 위해 id 설정
    const h3 = editorEl.querySelectorAll('h3');
    const h4 = editorEl.querySelectorAll('h4');
    for (let i = 0; i < h3.length; ++i) {
      h3[i].id = h3[i].innerText.trim();
    }
    for (let i = 0; i < h4.length; ++i) {
      h4[i].id = h4[i].innerText.trim();
    }

    // 하이라이트 된 code태그 초기화
    const code = document.querySelectorAll('pre code');
    for (let i = 0; i < code.length; ++i) {
      code[i].removeAttribute('class');
      code[i].removeAttribute('id');
      code[i].innerHTML = code[i].innerText;
    }

    /********************************************************************************
      이미지 업로드
      에디터에 base64 형식으로 저장된 이미지 태그의 src를
      서버에 업로드 후 받은 url로 대체한다.
    ********************************************************************************/
    const images = this.context.image._infoList;
    for (let i = 0; i < images.length; ++i) {

      // 게시글 수정 시
      // 저장된 이미지는 제외하고 실행
      if (oldImages.indexOf(images[i].src) > -1) {
        oldImages.splice(oldImages.indexOf(images[i].src), 1);
        continue;
      }

      const url = await fetch(images[i].src)
        .then(res => res.blob())
        .then(async blob => {
          const fileInfo = {
            name: images[i].name,
            type: 'image',
            format: blob.type.replace('image/', ''),
          };
          const infoBlob = new Blob([JSON.stringify(fileInfo)], {
            type: 'application/json'
          });
          const fd = new FormData();
          fd.append('file', blob);
          fd.append('info', infoBlob);

          // return url
          return fetch('/api/files', {
            method: 'POST',
            body: fd
            })
            .then(res => res.json())
            .then(data => {
              if (data.success) {
                return data.response.url;
              }
            })
            .catch(err => {
              console.warn(err);
            });
        });
      images[i].element.setAttribute('src', url);
    }

    // json body로 들어갈 객체
    const requestPost = {
      title: validPost.title,
      categoryId: validPost.categoryId,
      content: this.getContents().replace(/ ?class=\"(se-|_+se)[^\"]*(\'|\")| ?contenteditable=\"(true|false)\"/ig, ''),
      desc: editorEl.textContent.substring(0, Math.min(499, editorEl.textContent.length)),
      visible: validPost.visible
    };

    fetch('/api/posts' + (validPost.postId > -1 ? `/${validPost.postId}` : ''), {
      method: validPost.postId > -1 ? 'PATCH' : 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(requestPost)
    })
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          window.removeEventListener('beforeunload', alertWhenLeave);
          setTimeout(() => {
            location.href = `/${data.response.id}`;
          }, 300);
        }
      });
    this.closeLoading();
  },
};
