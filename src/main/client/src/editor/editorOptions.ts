import SetOptions from 'suneditor-react/dist/types/SetOptions';
import editorPlugins from './plugins';

export default {
  buttonList: [
    ['_submit', '_code', '_hr'],
    ['font', 'fontSize', 'formatBlock'],
    ['blockquote'],
    ['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript'],
    ['fontColor', 'hiliteColor'],
    ['removeFormat'],
    ['outdent', 'indent'],
    ['align',],
    ['table', 'link', 'image'],
    ['preview']
  ],
  showPathLabel: true,
  resizingBar: true,
  resizeEnable: false,
  charCounter: true,
  width: '100%',
  minHeight: '700px',
  className: 'post-editor-area',
  font: [ // font(글꼴) 버튼
    'Arial',
    'D2Coding',
    'tohoma',
    'Courier New,Courier'
  ],
  fontSize: [ // size(크기) 버튼
    10, 14, 16, 18, 20, 24, 30, 36
  ],
  formats: [ // formats(문단 형식) 버튼
    'p', 'div', 'blockquote', 'pre', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  ],
  imageRotation: false,
  linkTargetNewWindow: true,
  plugins: editorPlugins
} as SetOptions
