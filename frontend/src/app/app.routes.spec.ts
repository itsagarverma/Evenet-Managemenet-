import { routes } from './app.routes';
describe('application routes', () => {
  it('uses a reusable dynamic gallery route and protected admin route', () => {
    expect(routes.some(route => route.path === 'our-work/:slug')).toBeTrue();
    expect(routes.some(route => route.path === 'admin' && !!route.canActivate)).toBeTrue();
    expect(routes.some(route => route.path === 'admin/login')).toBeTrue();
  });
});
