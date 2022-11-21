import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/dist/query";
import CategoryTreeSlice from '../slices/CategoryTreeSlice';
import PostSlice from '../slices/PostSlice';

export interface ApiResponse<T> {
  success: boolean;
  response: T | null;
  error: {
    message: string,
    status: number
  } | null;
}

const store = configureStore({
  // 각 slice의 reducer 들이 들어간다.
  reducer: {
    categoryTree: CategoryTreeSlice.reducer,
    post: PostSlice.reducer,
  }
});

setupListeners(store.dispatch);

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch;

export default store
