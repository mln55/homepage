import React, { useEffect, useRef, useState } from 'react';
import { MdCheckBox, MdCheckBoxOutlineBlank } from 'react-icons/md';
import { Link, useSearchParams } from 'react-router-dom';
import PostChangeOption from '../components/PostChangeOption';
import PostItem from '../components/PostItem';
import PostPagination from '../components/PostPagination';
import PostViewOption from '../components/PostViewOption';
import { useDispatch, useSelector } from '../hooks/index';
import { CategoryApiResponse } from '../slices/CategoryTreeSlice';
import { PostSliceActions } from '../slices/PostSlice';

function ManagePost() {

  const postList = useSelector(state => state.post.postList);
  const allChecked = useSelector(state => state.post.allChecked);
  const anyChecked = useSelector(state => state.post.checkedPostIds).length > 0;
  const totalCount = useSelector(state => state.post.totalCount);
  const [categoryTree, setCategoryTree] = useState<CategoryApiResponse[]>([]);
  const [searchParams] = useSearchParams();
  const paramPage = searchParams.get('page') || '1';
  const paramVisible = searchParams.get('visible');
  const paramCategoryId = searchParams.get('categoryid');

  const dispatch = useDispatch();

  const refChange = useRef<HTMLDivElement>(null);
  const refSearch = useRef<HTMLDivElement>(null);

  const [isChangeOpen, setIsChangeOpen] = useState<boolean>(false);
  const [isSearchOpen, setIsSearchOpen] = useState<boolean>(false);

  const getQueryString = () => {
    const validParams = ['categoryid', 'page', 'visible'];
    const paramSet = new Set();
    const params: string[] = [];

    searchParams.forEach((v, k) => {
      if (validParams.indexOf(k) < 0) return;
      if (paramSet.has(k)) return;

      paramSet.add(k);
      params.push(`${k}=${v}`);
    });

    return params.length > 0 ? '?' + params.join('&') : '';
  }

  const fetchPostList = () => {
    (async () => {
      const param = paramVisible ? `&visible=${paramVisible}` : '';
      const data = await fetch('/api/categories?count=post' + param).then(res => res.json());
      if (data.success) {
        const categoryList: CategoryApiResponse[] = data.response;
        setCategoryTree(categoryList);

        let totalCount = 0;
        if (paramCategoryId) {
          categoryList.forEach(p => {
            if ('' + p.categoryId === paramCategoryId) {
              totalCount = p.postsCount;
              return;
            }
            if (p.childList) {
              p.childList.forEach(c => {
                if ('' + c.categoryId === paramCategoryId) {
                  totalCount = c.postsCount;
                  return;
                }
              });
            }
          });
        } else {
          totalCount = categoryList.reduce((acc: number, d: {postsCount: number}) => acc + d.postsCount, 0);
        }
        dispatch(PostSliceActions.setTotalCount(totalCount));
      }
    })();

    (async () => {
      const qs = getQueryString();

      const uri = '/api/posts' + qs;

      const data = await fetch(uri).then(res => res.json());
      if (data.success) {
        dispatch(PostSliceActions.setPostList(data.response));
      }
    })();
  }

  useEffect(() => {
    fetchPostList();
  }, [paramCategoryId, paramPage, paramVisible]);

  useEffect(() => {
    const handleClickOutsideChange = (e: MouseEvent) => {
      if (refChange && e.target instanceof Node && !refChange.current?.contains(e.target)) {
        refChange.current?.classList.remove('open');
        setIsChangeOpen(false);
      }
    }
    const handleClickOutsideSearch = (e: MouseEvent) => {
      if (refSearch && e.target instanceof Node && !refSearch.current?.contains(e.target)) {
        refSearch.current?.classList.remove('open');
        setIsSearchOpen(false);
      }
    }

    document.addEventListener('click', handleClickOutsideChange, true);
    document.addEventListener('click', handleClickOutsideSearch, true);

    return () => {
      document.removeEventListener('click', handleClickOutsideChange, true);
      document.removeEventListener('click', handleClickOutsideSearch, true);
    };
  }, []);

  const handleToggleCheckAll = () => {
    if (allChecked) dispatch(PostSliceActions.uncheckAll());
    else dispatch(PostSliceActions.checkAll());
  }

  const handleToggleOpen = (isOpen: boolean, setIsOpen: (value: boolean) => void, ref: React.RefObject<HTMLDivElement>) => {
    if (isOpen) {
      ref.current?.classList.remove('open');
    } else {
      ref.current?.classList.add('open');
    }
    setIsOpen(!isOpen);
  }

  return (
    <div className="post-container">
      <div className="post-list-button">
        <div className="check">
          <input id="postcheckall" type="checkbox" checked={allChecked} onChange={handleToggleCheckAll}/>
          {/* <label htmlFor="postcheckall">
            {allChecked ? <MdCheckBox /> : <MdCheckBoxOutlineBlank />}
          </label> */}
        </div>
        <PostChangeOption
          isActivate={anyChecked}
          categoryTree={categoryTree}
          el={refChange}
          onToggleOpen={() => handleToggleOpen(isChangeOpen, setIsChangeOpen, refChange)}
        />
        <PostViewOption
          categoryTree={categoryTree}
          el={refSearch}
          onToggleOpen={() => handleToggleOpen(isSearchOpen, setIsSearchOpen, refSearch)}
          paramCategoryId={paramCategoryId}
          paramVisible={paramVisible}
        />
        <div className="new">
          <button onClick={() => window.location.href="/admin/manage/newpost"}>글 등록</button>
        </div>
      </div>
      {
        postList.length < 1
        ? <div>
          <h1>등록된 게시글이 없습니다.</h1>
          <Link to="/admin/manage/newpost" reloadDocument={true}>게시글 등록하기</Link>
        </div>
        : <ul className='post-list'>
          {
          postList.map(post => <PostItem key={post.id} post={post} recivePostList={fetchPostList}/>)
          }
        </ul>
      }
      <PostPagination
        page={paramPage ? Number(paramPage) : 1}
        paramCategoryId={paramCategoryId}
        paramVisible={paramVisible}
      />
    </div>
  )
}

export default ManagePost
