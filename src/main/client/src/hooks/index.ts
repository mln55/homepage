import { AppDispatch, RootState } from '../store/index';
import { TypedUseSelectorHook, useDispatch as useDispatchOrigin, useSelector as useSelectorOrigin } from 'react-redux';

export const useDispatch: () => AppDispatch = useDispatchOrigin;
export const useSelector: TypedUseSelectorHook<RootState> = useSelectorOrigin;
