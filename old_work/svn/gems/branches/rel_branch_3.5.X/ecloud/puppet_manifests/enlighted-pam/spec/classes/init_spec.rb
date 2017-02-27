require 'spec_helper'
describe 'pam' do

  context 'with defaults for all parameters' do
    it { should contain_class('pam') }
  end
end
