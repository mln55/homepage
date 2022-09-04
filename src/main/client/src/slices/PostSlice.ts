import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export type CategoryApiNameResponse = {
  name: string;
  parent: string | null;
}

export type PostApiResponse = {
  id: number;
  title: string;
  category: CategoryApiNameResponse;
  content: string;
  desc: string;
  visible: boolean;
  hit: number;
  postAt: string;
}

export type PostState = {
  totalCount: number;
  postList: PostApiResponse[];
  allChecked: boolean;
  checkedPostIds: number[];
}

const PostSlice = createSlice({
  name: 'postSlice',
  initialState: {
    totalCount: 0,
    postList: [],
    allChecked: false,
    checkedPostIds: [],
  } as PostState,
  reducers: {

    setTotalCount: (state: PostState, action: PayloadAction<number>) => {
      state.totalCount = action.payload;
    },

    setPostList: (state: PostState, action: PayloadAction<PostApiResponse[]>) => {
      state.postList = action.payload;
    },

    deletePost: (state: PostState, action: PayloadAction<number>) => {
      const index = state.postList.findIndex(p => p.id === action.payload);
      if (index > -1) state.postList.splice(index, 1);
    },

    checkAll: (state: PostState) => {
      state.allChecked = true;
      state.postList.forEach(p => {
        if (state.checkedPostIds.indexOf(p.id) < 0) {
          state.checkedPostIds.push(p.id);
        }
      });
    },

    uncheckAll: (state: PostState) => {
      state.allChecked = false;
      state.checkedPostIds = [];
    },

    checkOne: (state: PostState, action: PayloadAction<number>) => {
      if (state.checkedPostIds.indexOf(action.payload) < 0) state.checkedPostIds.push(action.payload);
      if (state.postList.length === state.checkedPostIds.length) state.allChecked = true;
    },

    uncheckOne: (state: PostState, action: PayloadAction<number>) => {
      state.checkedPostIds.splice(state.checkedPostIds.indexOf(action.payload), 1);
      if (state.allChecked) state.allChecked = false;
    },

    updatePost: (state: PostState, action: PayloadAction<{
      id: number,
      visible?: boolean,
      category?: CategoryApiNameResponse
    }>) => {
      const { id, visible, category } = action.payload;
      if (visible === undefined && category === undefined) return;

      const idx = state.postList.findIndex(p => p.id === id);
      const post = idx > -1 ? state.postList[idx] : undefined;
      if (!post) return;

      if (visible !== undefined) post.visible = visible;
      if (category !== undefined) post.category = category;

    }
  }
});

export const PostSliceActions = PostSlice.actions
export default PostSlice;
