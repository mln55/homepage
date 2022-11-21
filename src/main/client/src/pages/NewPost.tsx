import hljs from 'highlight.js';
import React, { ChangeEvent, useEffect, useRef, useState } from 'react';
import { FaCheckSquare, FaSquare } from "react-icons/fa";
import { useParams } from 'react-router-dom';
import SunEditor from "suneditor-react";
import "suneditor/dist/css/suneditor.min.css";
import SuneditorCore from "suneditor/src/lib/core";
import editorOptions from '../editor/editorOptions';
import { alertWhenLeave, oldImages, validPost } from '../editor/plugins/submit';
import { CategoryApiResponse } from '../slices/CategoryTreeSlice';
import { PostApiResponse } from '../slices/PostSlice';

function NewPost() {

  const { postIdStr } = useParams();
  const editor = useRef<SuneditorCore>();
  const [title, setTitle] = useState<string>('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [content, setContent] = useState<string>('');
  const [visible, setVisible] = useState<boolean>(false);
  const [categoryList, setCategoryList] = useState<CategoryApiResponse[]>([]);

  useEffect(() => {
    (async () => {
      const data = await fetch('/api/categories?count=post').then(res => res.json());

      if (!data.success) return;

      if (data.response.length < 1) {
        alert('등록된 카테고리가 없습니다. 카테고리를 먼저 등록해주세요.');
        location.href = '/admin/manage/category';
      } else {
        const catResponse: CategoryApiResponse[] = data.response;
        setCategoryList(catResponse);
        setCategoryId('' + catResponse[0]?.categoryId);
        validPost.categoryId = '' + catResponse[0]?.categoryId;

        if (!postIdStr) return;

        if (!/^\d+$/.test(postIdStr)) alertInvalidPost();

        // 기존 포스트 수정
        const data2 = await fetch(`/api/posts/${postIdStr}`).then(res => res.json());

        if (!data2.success) alertInvalidPost();

        validPost.postId = Number(postIdStr);

        const post: PostApiResponse = data2.response;
        setTitle(post.title);
        validPost.title = post.title;

        editor.current?.insertHTML(post.content, true);
        setContent(post.content);
        validPost.content = post.content;
        editor.current?.core.context['image']._infoList.forEach((i: { src: any; }) => {
          oldImages.push(i.src)
        });
        hljs.highlightAll();

        setVisible(post.visible);
        validPost.visible = post.visible;

        const postCategoryId = post.category.parent
          ? catResponse.find(p => p.name === post.category.parent)?.childList.find(c => c.name === post.category.name)?.categoryId
          : catResponse.find(p => p.name === post.category.name)?.categoryId;
        setCategoryId('' + postCategoryId);
        validPost.categoryId = '' + postCategoryId;
      }
    })();
    window.addEventListener('beforeunload', alertWhenLeave);

    return () => {
      window.removeEventListener('beforeunload', alertWhenLeave);
    }
  }, []);

  const alertInvalidPost = () => {
    alert('잘못된 주소이거나 존재하지 않는 게시글입니다.');
    history.back();
  }

  const getSunEditorInstance = (suneEditor: SuneditorCore) => {
    editor.current = suneEditor;
  }

  const handleChangeTitle = (e: ChangeEvent<HTMLInputElement>) => {
    setTitle(e.target.value);
    validPost.title = e.target.value;

  }

  const handleChangeEditorContents = (contents: any) => {
    setContent(contents);
    validPost.content = contents;
  }

  const handleChangeCategory = (e: ChangeEvent<HTMLSelectElement>) => {
    const selected = e.target.selectedOptions[0];
    setCategoryId(selected?.value!);
    validPost.categoryId = selected?.value!;
  }

  const handleToggleVisible = () => {
    setVisible(!visible);
    validPost.visible = !visible
  }

  return (
    <div>
      <form onSubmit={(e) => e.preventDefault()}>
        <span>제목: </span><input onChange={handleChangeTitle} type="text" name="title" value={title}/>
        <span>카테고리:</span>
        <select value={categoryId} onChange={handleChangeCategory}>
        {
          categoryList.map(p =>
            <React.Fragment key={p.categoryId}>
            <option value={p.categoryId}>{p.name}</option>
            {
              p.childList.map(c =>
                <option value={c.categoryId} key={c.categoryId}>{p.name}/{c.name}</option>
              )
            }
            </React.Fragment>
          )
        }
        </select>
        <div onClick={handleToggleVisible}>{visible ? <FaSquare /> : <FaCheckSquare />}<span>숨김 포스트로 등록</span></div>
        <input type="hidden" name="category" value={categoryId}/>
        <input type="hidden" name="content" value={content}/>
        <input type="hidden" name="visible" value={String(visible)}/>
      </form>
      <SunEditor
        onChange={handleChangeEditorContents}
        getSunEditorInstance={getSunEditorInstance}
        lang={'ko'}
        setAllPlugins={true}
        setOptions={editorOptions}
      />
    </div>
  )
}

export default NewPost
